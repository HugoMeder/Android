package de.bitsnarts.camerasnapshot;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.ImageReader;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executor;

import javax.microedition.khronos.opengles.GL10;

import de.bitsnarts.CameraService.CameraTask;
import de.bitsnarts.android.utils.LogUtils;
import de.bitsnarts.android.utils.communication.BNAPrintlnService;

import static android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener {

    private CameraCaptureSession cameraCaptureSession;
    private CameraDevice camera;
    private TextureView textureView;
    private boolean surfaceTextureAvailable;
    private Surface targetSurface;
    private ImageReader imgReader;
    private int surfaceTextureWidth;
    private int surfaceTextureHeight;
    private SurfaceTexture surfaceTexture;
    private CaptureRequest req;
    private Callback cb;
    private CaptureHandler ch;
    private boolean contiousCapture = false ;
    private CaptureRequest.Builder builder;
    private CameraManager manager;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //log ( "textureAvailable... " ) ;
        /*ByteBuffer pixelBuf = ByteBuffer.allocateDirect(4*surfaceTextureWidth*surfaceTextureHeight); // TODO - reuse this
        GLES20.glReadPixels(0, surfaceTextureWidth, surfaceTextureHeight, 1, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixelBuf);
        byte[] a = pixelBuf.array();;
        log ( "textureAvailable "+surfaceTextureWidth+", "+surfaceTextureHeight+" byte "+a[0] ) ;
        //surfaceTexture.releaseTexImage();
        */
    }

    class MyOnImageAvailableListener implements ImageReader.OnImageAvailableListener {

        @Override
        public void onImageAvailable(ImageReader imageReader) {

            log ( "image available" ) ;
        }
    }

    class Callback extends CameraCaptureSession.CaptureCallback {

        int index ;

        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            log ( "captureCompleted "+index++ );
            captured ( result ) ;
            if ( !contiousCapture ) {
                startCapture();
            }
        }
    }

    class CaptureHandler extends Handler {

        public void handleMessage(Message msg) {
            log ( "handleMessage "+msg ) ;
        }
    }


    class StateCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            log ( "onConfigured" ) ;
            setCaptureSession ( cameraCaptureSession ) ;
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            log ( "onConfigureFailed" ) ;
        }
    }

    class Exec implements Executor {

        @Override
        public void execute(@NonNull Runnable runnable) {
            //println ( "execute("+runnable+")" ) ;
            runnable.run();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height ) {
        synchronized ( this ) {
            surfaceTextureAvailable = true ;
            surfaceTextureWidth = width ;
            surfaceTextureHeight = height ;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tryCameraConfig () ;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height ) {
        synchronized ( this ) {
            surfaceTextureAvailable = true ;
            surfaceTextureWidth = width ;
            surfaceTextureHeight = height ;
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    class CameraCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            synchronized ( this ) {
                camera = cameraDevice;
            }
            tryCameraConfig () ;
            log ( "camera opened" ) ;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            log ( "camera disconnected" ) ;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            log ( "camera error, code "+i ) ;
        }

    }

    class CameraHandler extends Handler {
 /*
        public void dispatchMessage(Message msg) {
            super.dispatchMessage( msg );
            cameraEvent ( msg ) ;
            textView.setText("message") ;
        }
*/
    }

    static int nextInstanceNr ;
    private final int instanceNr;
    private TextView textView;
    private static BNAPrintlnService println = new BNAPrintlnService() ;
    private CameraCallback cameraCallback = new CameraCallback () ;
    private CameraHandler cameraHandler = new CameraHandler () ;

    MainActivity () {
        super () ;
        this.instanceNr = nextInstanceNr++ ;
    }

    private void log(String s) {
        println.println( s );
        System.out.println ( s ) ;
    }

    @TargetApi(Build.VERSION_CODES.P)
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textView.setText ( "onCreate, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
        log(  "onCreate, instance "+instanceNr+"\nprintln-state="+println.getState() );
        getCamera () ;
        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener( this );
        System.out.println ( "hello" ) ;
    }

    @Override
    protected void onStart() {
        super.onStart();
        startBackgroundThread () ;
        log ( "onStart, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @Override
    protected void onResume() {
        super.onResume();
        log ( "onResume, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @Override
    protected void onPause() {
        super.onPause();
        log ( "onPause, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopBackgroundThread () ;
        log ( "onStop, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log ( "onDestroy, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void getCamera() {
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String id = null;
        try {
            id = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
            textView.setText( e.toString() );
            return ;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                if ( true )
                    manager.openCamera(id, cameraCallback, cameraHandler );
            } catch (CameraAccessException e) {
                e.printStackTrace();
                log( e.toString() );
            }
        } else {
            log ( "camera not granted" );
        }
    }

    private void tryCameraConfig() {
        synchronized ( this ) {
            if ( camera == null || !surfaceTextureAvailable ) {
                return  ;
            }
        }
        log ( "configureCamera ..." ) ;
        Vector<OutputConfiguration> configs = new Vector<OutputConfiguration>();
        if ( false ) {
            surfaceTexture = textureView.getSurfaceTexture() ;
            targetSurface = new Surface( surfaceTexture ) ;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configs.add ( new OutputConfiguration( targetSurface ) ) ;
            }
        }
        int width = 640;
        int height = 480;
        CameraCharacteristics characteristics = null;
        try {
            characteristics = manager.getCameraCharacteristics(camera.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if ( true ) {
//            imgReader = ImageReader.newInstance( this.surfaceTextureWidth, this.surfaceTextureHeight, ImageFormat.PRIVATE, 1 ) ;
            log ( "imgReader, width "+width+", height "+height ) ;
            imgReader = ImageReader.newInstance( width, height, ImageFormat.JPEG, 2 ) ;

            imgReader.setOnImageAvailableListener( new MyOnImageAvailableListener(), null );
        }
        SessionConfiguration config = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            config = new SessionConfiguration(  SESSION_REGULAR, configs, new Exec(), new StateCallback()  );
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                camera.createCaptureSession(config);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        try {
//            builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW );
            builder = camera.createCaptureRequest( CameraDevice.TEMPLATE_STILL_CAPTURE );
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            builder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

        } catch (CameraAccessException e) {
            log ( e.toString() ) ;
            return ;
        }
        /*
        if ( targetSurface != null ) {
            builder.addTarget(targetSurface);
            //surfaceTexture.setOnFrameAvailableListener ( this ) ;
        }*/
        if ( imgReader != null )
            builder.addTarget( imgReader.getSurface() );
        //session.capture( req,  new Callback(),  ) ;
        //cameraCaptureSession.setRepeatingRequest( req, new Callback(), new CaptureHandler() ) ;
        cb = new Callback();
        ch = new CaptureHandler();
        startCapture () ;
    }

    private void setCaptureSession(CameraCaptureSession cameraCaptureSession) {
        synchronized ( this ) {
            this.cameraCaptureSession = cameraCaptureSession ;
        }
    }

    private void startCapture() {
        log ( "start capture..." ) ;
        try {
            req = builder.build();
            //android.hardware.camera2.CaptureRequest.convertSurfaceToStreamId ( req ) ;
            //req.convertSurfaceToStreamId () ;
            if ( !contiousCapture ) {
                cameraCaptureSession.capture( req, cb, mBackgroundHandler ) ;
            } else {
                cameraCaptureSession.setRepeatingRequest( req, cb, mBackgroundHandler ) ;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            log (LogUtils.exeptionToStr( e )) ;
        }

    }

    private void captured(TotalCaptureResult result) {
        /*
        if (Build.VERSION.SDK_INT >= 29) {
            SurfaceTexture st = textureView.getSurfaceTexture();
            log ( "captured " ) ;
            //log ( "textureAvailable... " ) ;
            ByteBuffer pixelBuf = ByteBuffer.allocateDirect(4*surfaceTextureWidth*surfaceTextureHeight); // TODO - reuse this
            GLES20.glReadPixels(0, 0, surfaceTextureWidth, surfaceTextureHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixelBuf);
            byte[] a = pixelBuf.array();;
            log ( "textureAvailable "+surfaceTextureWidth+", "+surfaceTextureHeight+" byte "+a[3] ) ;
            }
        */

    }

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

}
