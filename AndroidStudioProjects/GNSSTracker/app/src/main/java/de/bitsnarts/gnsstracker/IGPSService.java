package de.bitsnarts.gnsstracker;

public interface IGPSService {
    void requestCallbacks () ;
    int getNumLogsDone () ;
    int getNumGnssChanges () ;
}
