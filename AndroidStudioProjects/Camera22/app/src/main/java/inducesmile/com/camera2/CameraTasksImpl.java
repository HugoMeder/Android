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

import static android.os.Environment.DIRECTORY_PICTURES;

// import android.util.Log;

public class CameraTasksImpl implements CameraTasks {
    private static final String TAG = "AndroidCameraApi";
    private static BNAPrintlnService log = new BNAPrintlnService () ;
    private LogStub Log = new LogStub () ;

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

    class PreviewAvailableListener implements ImageReader.OnImageAvailableListener {

        @Override
        public void onImageAvailable(ImageReader imageReader) {
            //log("onImageAvailable...");
            try {
                Image img = imageReader.acquireLatestImage();
                if ( img != null ) {
                    //log("preview img != null");
                    Image.Plane planes = img.getPlanes()[0];;
                    if ( planes == null ) {
                        //log ( "planes==null" ) ;
                        img.close () ;
                        return ;
                    }
                    ByteBuffer buffer = planes.getBuffer() ;
                    if ( buffer != null ) {
                        //log("buffer != null");
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get( bytes ) ;
                        if ( bytes != null ) {
                            log("buffer length "+bytes.length );
                            updatePreview () ;
                        } else {
                            log ( "bytes == null" ) ;
                        }
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
    class LogStub {
        void e ( String tag, String txt ) {
            log.println( "Log "+tag+" "+txt );
        }
    }

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

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            //Log.e(TAG, "onOpened");
            log ( "onOpened" ) ;
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            log ( "onDisconnected" ) ;
            camera.close();
            cameraDevice = null;
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            log ( "onError" ) ;
            if ( camera != null )
                camera.close();
            cameraDevice = null;
        }
    };
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
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
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

    protected void takePicture() {
        if(null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
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
            file = new File(Environment.getExternalStoragePublicDirectory( DIRECTORY_PICTURES )/*.getExternalStorageDirectory()*/+"/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        log ( "acquireLatestImage "+image ) ;
                        //MediaStore.Images.Media.insertImage ( getContentResolver(), image, "pic.jpeg", "snapshot" ) ;

                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        log ( "buffer "+buffer ) ;
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
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
                    Toast.makeText(activity, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
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

    private void log(String s) {
        log.println( s );
    }


    protected void createCameraPreview() {
        try {
            log ( "createCameraPreview..." ) ;
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            if ( jpegPreviewSize != null ) {
                previewImageReader = ImageReader.newInstance ( jpegPreviewSize.getWidth(), jpegPreviewSize.getHeight(), ImageFormat.JPEG, 10 ) ;
                previewImageReader.setOnImageAvailableListener( new PreviewAvailableListener(), null );
                captureRequestBuilder.addTarget(previewImageReader.getSurface());
            }
            Vector<Surface> targets = new Vector<Surface>() ;
            targets.add( surface ) ;
            if ( previewImageReader != null ) {
                targets.add( previewImageReader.getSurface() ) ;
            }
            log ( "createCaptureSession..." ) ;
            cameraDevice.createCaptureSession( targets, new CameraCaptureSession.StateCallback(){@Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    log ( "onConfigured" ) ;
                    if (null == cameraDevice) {
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
            }, null);
            log ( "createCaptureSession, done" ) ;
        } catch (Throwable e) {
            log ( LogUtils.exeptionToStr( e ) ) ;
            e.printStackTrace();
        }
    }
    public void openCamera() {
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            Size[] outSizes = map.getOutputSizes( ImageFormat.JPEG );
            if ( outSizes != null ) {
                log ( "Output-Sizes" ) ;
                for ( Size s : outSizes ) {
                    log ( "\t"+s.getWidth()+" "+s.getHeight() ) ;
                }
                jpegPreviewSize = outSizes[outSizes.length-1] ;
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
        Log.e(TAG, "openCamera X");
    }
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
       captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            //cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
            previewCameraCaptureSession.capture( captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

}