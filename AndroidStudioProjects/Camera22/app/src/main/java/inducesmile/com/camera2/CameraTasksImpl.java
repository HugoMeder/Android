package inducesmile.com.camera2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import inducesmile.communication.BNAPrintlnService;
import inducesmile.communication.LogUtils;

import android.util.Log;
import static android.os.Environment.DIRECTORY_PICTURES;

// import android.util.Log;

public class CameraTasksImpl implements CameraTasks {
    private CommunicationThread comunication;
    private static BNAPrintlnService log = new BNAPrintlnService();
    //private LogStub Log = new LogStub () ;

    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession previewCameraCaptureSession;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Size jpegPreviewSize;
    private ImageReader previewImageReader;
    private AppCompatActivity activity;
    private boolean repeatingPreviewCapture = true;
    private boolean useTextureView = true;
    private FrameBufferQueue previewBuffer = new FrameBufferQueue();
    private FrameBufferQueue imageBuffer = new FrameBufferQueue();

    private PreviewCaptureCallback previewCaptureCallback = new PreviewCaptureCallback();

    class StartCsptureCmd implements Runnable {

        @Override
        public void run() {
            startFullCaptrure();
        }
    }

    class MyHandler extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                super.handleMessage(msg);
            } catch (Throwable th) {
                log(LogUtils.exeptionToStr(th));
            }
        }
    }


    CameraTasksImpl() {
        comunication = new CommunicationThread(this);
        int vers = 12;
        comunication.println("Hello from CameraTasksImpl, " + vers);
        log("Hello from CameraTasksImpl, " + vers);
        startBackgroundThread();
    }

    class PreviewCaptureCallback extends CameraCaptureSession.CaptureCallback {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            //log ( "onCaptureCompleted" ) ;
            //if ( !repeatingPreviewCapture )
            //updatePreview () ;
        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            log("onCaptureSequenceAborted");
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session,
                                               int sequenceId,
                                               long frameNumber) {
            log("onCaptureSequenceCompleted");
            //mBackgroundHandler.post(new StartCsptureCmd());
        }
    }

    class PreviesStateCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            log("onConfigured(preview)");
            if (null == cameraDevice) {
                log("null == cameraDevice");
                return;
            }
            // When the session is ready, we start displaying the preview.
            previewCameraCaptureSession = cameraCaptureSession;
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            Toast.makeText(activity, "Configuration change", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onClosed (CameraCaptureSession session) {
            log ( "preview session closed" ) ;
            mBackgroundHandler.post(new StartCsptureCmd());
        }
    }

    class PreviewAvailableListener implements ImageReader.OnImageAvailableListener {

        @Override
        public void onImageAvailable(ImageReader imageReader) {
            //log("onImageAvailable...!");
            try {
                //Image img = imageReader.acquireLatestImage();
                Image img = imageReader.acquireNextImage() ;
                if ( img != null ) {
                    //log("preview img != null");
                    Image.Plane planes = img.getPlanes()[0];;
                    if ( planes == null ) {
                        log ( "planes==null" ) ;
                        img.close () ;
                        return ;
                    }
                    ByteBuffer buffer = planes.getBuffer() ;
                    int bl = buffer.capacity() ;
                    if ( buffer != null ) {
                        log("get buffer,  length "+bl );
                        FrameBufferQueue.JPegFrameBuffer buf = previewBuffer.getBuffer(bl, jpegPreviewSize.getWidth(), jpegPreviewSize.getHeight());
                        byte[] bytes = buf.getBuffer() ;
                        buffer.get( bytes, 0, bl ) ;
                        log("buffer length "+bl );
                        comunication.previewImage( buf );
                    } else {
                        log ( "buffer == null" ) ;
                    }
                    //log("img.close ...");
                    img.close();
                    //log("img.closed");
                }
                else
                    log ( "preview img == null" ) ;
            } catch ( Throwable th ) {
                log ( LogUtils.exeptionToStr( th ) ) ;
            }
        }
    }
    /*
    class LogStub {
        void e ( String tag, String txt ) {
            log.println( "Log "+tag+" "+txt );
        }
    }
*/
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private final CameraDevice.StateCallback stateCallback;

    {
        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                //This is called when the camera is open
                //Log.e(TAG, "onOpened");
                log("onOpened");
                cameraDevice = camera;
                createCameraPreview();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                log("onDisconnected");
                camera.close();
                cameraDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                log("CameraDevice.StateCallback: onError, " +error );
                if (camera != null) {
                    log("close camera ...");
                    camera.close();
                }
                cameraDevice = null;
            }
        };
    }

    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //Toast.makeText(CameraTasks.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new MyHandler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setActivity ( AppCompatActivity activity ) {

        this.activity = activity ;
    }

    @Override
    public void startPreview() {
        try {
            createCameraPreview () ;
        }catch ( Throwable th ) {
            log ( LogUtils.exeptionToStr( th ) ) ;
        }
    }

    @Override
    public void setPreviewTexture(TextureView texturView ) {
        synchronized ( this ) {
            this.textureView = texturView ;
        }
    }

    void startFullCaptrure() {
       if(null == cameraDevice) {
            log ("cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            file = new File(Environment.getExternalStoragePublicDirectory( DIRECTORY_PICTURES )+"/pic.jpg") ;
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    image = reader.acquireLatestImage();
                    log ( "acquireLatestImage "+image ) ;
                    //MediaStore.Images.Media.insertImage ( getContentResolver(), image, "pic.jpeg", "snapshot" ) ;

                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    log ( "IMAGE buffer "+buffer ) ;
                    int bl = buffer.capacity();

                    FrameBufferQueue.JPegFrameBuffer buf = imageBuffer.getBuffer(bl, image.getWidth(), image.getHeight());
                    byte[] bytes = buf.getBuffer() ;
                    buffer.get( bytes, 0, bl ) ;
                    image.close();
                    comunication.image ( buf ) ;
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        file.getParentFile ().mkdirs() ;
                        log("write bytes to " + file.getAbsolutePath() ) ;
                        output = new FileOutputStream(file);
                        output.write(bytes);
                        log("bytes written " + bytes.length);
                    } catch ( Throwable th ) {
                        log (LogUtils.exeptionToStr( th ) ) ;
                    } finally {
                        if (null != output) {
                            output.close();
                            log ( "closed, file exists "+file.exists() ) ;
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(activity, "Picture captured", Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public void takePicture() {
        if ( previewCameraCaptureSession != null )
            try {
                previewCameraCaptureSession.close();
            } catch ( Throwable th ) {
                log ( LogUtils.exeptionToStr( th ) ) ;
            }

    }

    @Override
    public void log(String s) {
        Log.i ( "CameraTasksImpl",s ) ; //log.println( s );
    }

    @Override
    public Handler getHandler() {
        return  mBackgroundHandler;
    }


    protected void createCameraPreview() {
        try {
            log ( "createCameraPreview..." ) ;
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            if ( useTextureView  )
                captureRequestBuilder.addTarget(surface);
            if ( jpegPreviewSize != null ) {
                log ( "jpegPreviewSize "+jpegPreviewSize.getWidth()+"x"+jpegPreviewSize.getHeight() ) ;
                previewImageReader = ImageReader.newInstance ( jpegPreviewSize.getWidth(), jpegPreviewSize.getHeight(), ImageFormat.JPEG, 10 ) ;
                previewImageReader.setOnImageAvailableListener( new PreviewAvailableListener(), null );
                captureRequestBuilder.addTarget(previewImageReader.getSurface());
            } else {
                log ( "no ImageReader installed" ) ;
            }
            Vector<Surface> targets = new Vector<Surface>() ;
            if ( useTextureView )
                targets.add( surface ) ;
            if ( previewImageReader != null ) {
                targets.add( previewImageReader.getSurface() ) ;
                log ( "previewImageReader added" ) ;
            }
            log ( "createCaptureSession..." ) ;
            cameraDevice.createCaptureSession( targets, new PreviesStateCallback(), null);
            log ( "createCaptureSession, done" ) ;
        } catch (Throwable e) {
            log ( LogUtils.exeptionToStr( e ) ) ;
            e.printStackTrace();
        }
    }

    @Override
    public void openCamera() {
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        log ( "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            Size[] outSizes = map.getOutputSizes( ImageFormat.JPEG );
            if ( outSizes != null ) {
                log ( "Output-Sizes" ) ;
                int index = 0 ;
                for ( Size s : outSizes ) {
                    log ( "\t"+(index++)+" "+s.getWidth()+" "+s.getHeight() ) ;
                }
                jpegPreviewSize = outSizes[17/*outSizes.length-1*/] ;
            }

            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        log ( "openCamera done");
    }

    protected void updatePreview() {
        if(null == cameraDevice) {
            log ( "updatePreview error, return");
        }
       captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            if ( repeatingPreviewCapture )
                previewCameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), previewCaptureCallback, /*mBackgroundHandler*/ null );
            else
                previewCameraCaptureSession.capture( captureRequestBuilder.build(), previewCaptureCallback, /*mBackgroundHandler*/ null );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeCamera() {
        log ( "closeCamera ..." ) ;
        if (null != cameraDevice) {
            try {
                cameraDevice.close();
                cameraDevice = null;
            } catch ( Throwable th ) {
                log ( LogUtils.exeptionToStr( th )) ;
            }
            log ( "camera closed" ) ;
        } else {
            log ( "cameraDevice == null" ) ;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

}