package de.bitsnarts.android.gps.service;

import android.app.Service;
import android.content.Intent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

import de.bitsnarts.android.gpstest.LocationLogger;

import java.io.File;
import java.io.FileNotFoundException;

import de.bitsnarts.android.tools.logger.Logger;

public class GPSService extends Service implements LocationListener, IGPSService {

    IBinder binder = new MyBinder () ;
    private LocationManager locationManager;
    private static final int maxNUmConstellations = 7 ;
    private int satelliteCounts[] = new int[maxNUmConstellations];
    private int numLogsDone ;
    private int numGnssChanges ;
    private PollingThread thread = createThread () ;
    private LocationLogger locationLogger;
    private static final boolean useSingleUpdate = false ;

    private PollingThread createThread () {
        PollingThread rv = new PollingThread() ;
        rv.start(); ;
        return rv ;
    }
    private GnssStatus.Callback callback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            numGnssChanges++ ;
            int n = status.getSatelliteCount();
            for ( int i = 0 ; i < 6 ; i++ ) {
                satelliteCounts[i] = 0 ;
            }
            for ( int i = 0 ; i < n ; i++ ) {
                satelliteCounts[status.getConstellationType(i)]++ ;
            }
        }
    } ;

    class PollingThread extends Thread implements LocationListener {

        private boolean doRun ;
        private long lastLocationUpdate;

        @Override
        public void run() {
            if ( useSingleUpdate ) {
                Looper.prepare();
                lastLocationUpdate = System.currentTimeMillis() ;
            }
            for (;;) {
                synchronized ( this ) {
                    while ( !doRun ) {
                        try {
                            wait () ;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                try {
                    Thread.sleep ( 1000 ) ;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if ( useSingleUpdate ) {
                    long delta ;
                    long now ;
                    synchronized ( this ) {
                        now = System.currentTimeMillis() ;
                        delta = now - lastLocationUpdate;
                    }
                    if ( delta > 10000 ) {
                        lastLocationUpdate = now ;
                        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
                    }
                } else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    handleLocation ( location ) ;
                }
            }
        }

        public void doPoll () {
           synchronized ( this ) {
               doRun = true ;
               notifyAll();
           }
        }

        @Override
        public void onLocationChanged(Location location) {
            synchronized ( this ) {
                lastLocationUpdate = System.currentTimeMillis() ;
            }
            handleLocation ( location ) ;
        }

        void handleLocation ( Location location ) {
            if ( location != null ) {
                GPSService.this.onLocationChanged(location);
                Logger.log().println ( "lon "+location.getLongitude()+" lat "+location.getLatitude()+" q "+location.getAccuracy()+" numGnssChanges "+numGnssChanges ) ;
            } else {
                Logger.log().println ( "NO data" ) ;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        synchronized ( this ) {
            numLogsDone++ ;
            locationLogger.log ( location ) ;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void requestCallbacks() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        thread.doPoll();
 //       locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this);
        try {
            File filesDir = getFilesDir();;
            locationLogger = new LocationLogger ( filesDir ) ;
        } catch (FileNotFoundException e) {
            throw new Error ( e ) ;

        }

        locationManager.registerGnssStatusCallback ( callback ) ;
    }

    @Override
    public int getNumLogsDone() {
        synchronized ( this ) {
            return numLogsDone;
        }
    }

    @Override
    public int getNumGnssChanges() {
        return numGnssChanges;
    }

    public class MyBinder extends Binder {
        public IGPSService getService() {
            return GPSService.this ;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int rv = super.onStartCommand(intent, flags, startId);
        return rv ;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder ;
    }


}
