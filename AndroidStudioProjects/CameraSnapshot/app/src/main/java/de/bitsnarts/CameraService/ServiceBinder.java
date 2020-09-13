package de.bitsnarts.CameraService;

import android.os.Binder;
import android.os.IBinder;

import de.bitsnarts.CameraService.Model.ServiceModel;

public class ServiceBinder extends Binder {

    private final ServiceModel service;

    ServiceBinder (ServiceModel service ) {
        this.service = service ;
    }
    public ServiceModel getService() {
        return service ;
    }
}
