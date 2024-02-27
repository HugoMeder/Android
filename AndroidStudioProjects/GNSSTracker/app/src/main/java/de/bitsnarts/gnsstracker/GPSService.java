package de.bitsnarts.gnsstracker;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;

import de.bitsnarts.android.tools.logger.Logger;

public class GPSService implements LocationListener, IGPSService {

    private Context context;
    IBinder binder = new MyBinder();
    private LocationManager locationManager;
    private static final int maxNUmConstellations = 7;
    private int satelliteCounts[] = new int[maxNUmConstellations];
    private int numLogsDone;
    private int numGnssChanges;
    private PollingThread thread = createThread();
    private static final boolean useSingleUpdate = false;
    private LocationLogger locationLogger;

    public GPSService(Context context) {
        this.context = context;
    }

    private PollingThread createThread() {
        PollingThread rv = new PollingThread();
        rv.start();
        ;
        return rv;
    }

    GPSService() {

    }

    private GnssStatus.Callback callback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            numGnssChanges++;
            int n = status.getSatelliteCount();
            for (int i = 0; i < 6; i++) {
                satelliteCounts[i] = 0;
            }
            for (int i = 0; i < n; i++) {
                satelliteCounts[status.getConstellationType(i)]++;
            }
        }
    };

    class PollingThread extends Thread implements LocationListener {

        private boolean doRun;
        private long lastLocationUpdate;

        @Override
        public void run() {
            if (useSingleUpdate) {
                Looper.prepare();
                lastLocationUpdate = System.currentTimeMillis();
            }
            for (; ; ) {
                synchronized (this) {
                    while (!doRun) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (useSingleUpdate) {
                    long delta;
                    long now;
                    synchronized (this) {
                        now = System.currentTimeMillis();
                        delta = now - lastLocationUpdate;
                    }
                    if (delta > 10000) {
                        lastLocationUpdate = now;
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
                    }
                } else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    handleLocation(location);
                }
            }
        }

        public void doPoll() {
            synchronized (this) {
                doRun = true;
                notifyAll();
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            synchronized (this) {
                lastLocationUpdate = System.currentTimeMillis();
            }
            handleLocation(location);
        }

        void handleLocation(Location location) {
            if (location != null) {
                GPSService.this.onLocationChanged(location);
                Logger.log().println("lon " + location.getLongitude() + " lat " + location.getLatitude() + " q " + location.getAccuracy() + " numGnssChanges " + numGnssChanges);
            } else {
                Logger.log().println("NO data");
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
        synchronized (this) {
            numLogsDone++;
            locationLogger.log(location);
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
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        thread.doPoll();
        //       locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, this);
        try {
            File filesDir = context.getFilesDir();
            ;
            locationLogger = new LocationLogger(filesDir);
        } catch (FileNotFoundException e) {
            throw new Error(e);

        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.registerGnssStatusCallback(callback);
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

}
