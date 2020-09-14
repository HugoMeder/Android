package de.bitsnarts.camerasnapshot;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Vector;
import java.util.concurrent.Executor;

import de.bitsnarts.CameraService.CameraTask;
import de.bitsnarts.android.utils.communication.BNAPrintlnService;

import static android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private CameraCaptureSession cameraCaptureSession;
    private CameraDevice camera;
    private TextureView textureView;
    private boolean surfaceTextureAvailable;
    private Surface targetSurface;

    class Callback extends CameraCaptureSession.CaptureCallback {

        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {

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
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        synchronized ( this ) {
            surfaceTextureAvailable = true ;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tryCameraConfig () ;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

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
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        log ( "onStop, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log ( "onDestroy, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void getCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
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
        targetSurface = new Surface( textureView.getSurfaceTexture() ) ;
        OutputConfiguration outptConfig = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            outptConfig = new OutputConfiguration( targetSurface );
        }
        Vector<OutputConfiguration> configs = new Vector<OutputConfiguration>();
        configs.add ( outptConfig ) ;
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

    }

    private void setCaptureSession(CameraCaptureSession cameraCaptureSession) {
        synchronized ( this ) {
            this.cameraCaptureSession = cameraCaptureSession ;
        }
        startCapture () ;
    }

    private void startCapture() {
        log ( "start capture..." ) ;
        CaptureRequest.Builder builder = null;
        try {
            builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW );
            builder.addTarget( targetSurface );
            CaptureRequest req = builder.build();
            //session.capture( req,  new Callback(),  ) ;
            cameraCaptureSession.setRepeatingRequest( req, new Callback(), new CaptureHandler() ) ;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


}
