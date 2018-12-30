package de.bitsnarts.android.gps.service;

public interface IGPSService {
    void requestCallbacks () ;
    int getNumLogsDone () ;
    int getNumGnssChanges () ;
}
