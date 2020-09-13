package de.bitsnarts.CameraService;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.opengl.GLES20;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.Surface;
import android.view.SurfaceControl;

import java.util.Vector;
import java.util.concurrent.Executor;

import static android.hardware.camera2.params.SessionConfiguration.*;

public class CameraTask {

    private CameraDevice camera;
    private Vector<CameraTaskListener> listeners = new Vector<CameraTaskListener> () ;
    private CameraCaptureSession cameraCaptureSession;

    class Callback extends CameraCaptureSession.CaptureCallback {

        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {

        }
    }

    class StateCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            println ( "onConfigured" ) ;
            setCaptureSession ( cameraCaptureSession ) ;
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            println ( "onConfigureFailed" ) ;
        }
    }

    class Exec implements Executor {

        @Override
        public void execute(@NonNull Runnable runnable) {
            println ( "execute("+runnable+")" ) ;
            runnable.run();
        }
    }

    CameraTask () {
    }

    void init () {
    }

    @TargetApi(29)
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void setCamera(CameraDevice camera) {
        synchronized ( this ) {
            this.camera = camera ;
        }
        println ( "set camera "+camera ) ;
        if ( camera == null ) {
            println ( "camera == null" ) ;
            return;
        }
        println ( "startPreview" ) ;
        try {
            //int textureHandle[] = new int[1] ;
            //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            SurfaceControl.Builder b = new SurfaceControl.Builder () ;
            b.setBufferSize( 200, 200 ) ;
            b.setFormat( PixelFormat.RGB_888 ) ;
            b.setName( "mySCB" ) ;
            SurfaceControl sc = b.build();

            //SurfaceTexture tex = new SurfaceTexture( sc ) ;
            Surface surface = new Surface( sc ) ;
            OutputConfiguration outptConfig = new OutputConfiguration( surface ) ;
            Vector<OutputConfiguration> configs = new Vector<OutputConfiguration>();
            configs.add ( outptConfig ) ;
            SessionConfiguration config = new SessionConfiguration (  SESSION_REGULAR, configs, new Exec(), new StateCallback()  ) ;
            camera.createCaptureSession(config);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void addCameraTaskListener ( CameraTaskListener listener ) {
        synchronized ( listeners ) {
            listeners.add( listener ) ;
        }
    }

    public void removeCameraTaskListener ( CameraTaskListener listener ) {
        synchronized ( listeners ) {
            listeners.remove( listener ) ;
        }
    }

    void println ( String text ) {
        synchronized ( listeners ) {
            for ( CameraTaskListener l : listeners ) {
                l.println( text );
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.P)
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startPreviev() throws CameraAccessException {
        CameraCaptureSession session = null;;
        synchronized ( this ) {
            session = cameraCaptureSession ;
        }
        if ( session != null ) {
            CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW );
            CaptureRequest req = builder.build();
            session.capture( req,  new Callback(),  null ) ;
        }

    }

    private void setCaptureSession(CameraCaptureSession cameraCaptureSession) {
        synchronized ( this ) {
            this.cameraCaptureSession = cameraCaptureSession ;
        }
    }


}
