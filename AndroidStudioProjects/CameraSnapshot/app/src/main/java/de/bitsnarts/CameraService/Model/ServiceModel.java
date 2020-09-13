package de.bitsnarts.CameraService.Model;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;

import de.bitsnarts.CameraService.ServiceListener;
import de.bitsnarts.camerasnapshot.MainActivity;

public interface ServiceModel {

    void addServiceListener (  ServiceListener listener ) ;
    void removeServiceListener (  ServiceListener listener ) ;
    String getState();
    void start();
    void halt();
    void setCamera(CameraDevice cameraDevice);
    void startPreview() throws Exception;
    void setSurfaceTexture(SurfaceTexture surfaceTexture);
}
