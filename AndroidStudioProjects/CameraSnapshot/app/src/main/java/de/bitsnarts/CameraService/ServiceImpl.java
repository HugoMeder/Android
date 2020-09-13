package de.bitsnarts.CameraService;

import android.annotation.TargetApi;
import android.content.Intent;
import android.hardware.camera2.CameraDevice;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;

import java.util.Vector;

import de.bitsnarts.CameraService.Model.ServiceModel;

public class ServiceImpl extends android.app.Service implements ServiceModel, CameraTaskListener {

    ServiceBinder binder = new ServiceBinder ( this ) ;
    ServiceThread cooumication = new ServiceThread ( this ) ;
    private Vector<ServiceListener> listeners = new Vector<ServiceListener> () ;
    private String state = "initial" ;
    private CameraDevice camera;
    CameraTask cameraTask = new CameraTask () ;

    ServiceImpl() {
        cameraTask.addCameraTaskListener( this );
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public ServiceThread getCooumication() {
        return cooumication;
    }

    void setState ( String string ) {
        synchronized ( listeners ) {
            state = string ;
            for ( ServiceListener l : listeners ) {
                l.stateChanged( this );
            }
        }
    }

    @Override
    public void addServiceListener (  ServiceListener listener ) {
        synchronized ( listeners ) {
            listeners.add ( listener ) ;
        }
    }

    @Override
    public void removeServiceListener (  ServiceListener listener ) {
        synchronized ( listeners ) {
            listeners.remove ( listener ) ;
        }
    }

    @Override
    public String getState () {
        synchronized ( listeners ) {
            return state ;
        }
    }

    @Override
    public void start() {
        /*
        if ( cooumication.isRunning() )
            return ;
        new Thread (cooumication).start();
        */
    }

    @Override
    public void halt() {
        /*
        cooumication.halt();
        */
    }

    @Override
    public void setCamera(CameraDevice cameraDevice) {
        synchronized ( listeners ) {
            camera = cameraDevice ;
        }
        println ( "setCamera ( "+camera+")" ) ;
        cameraTask.setCamera ( camera ) ;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void startPreview() throws Exception {
        cameraTask.startPreviev () ;
    }

    @Override
    public void println(String text) {
        setState ( "camera: "+text ) ;
    }
}
