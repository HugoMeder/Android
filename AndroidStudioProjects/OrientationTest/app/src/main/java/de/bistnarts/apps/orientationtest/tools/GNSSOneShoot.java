package de.bistnarts.apps.orientationtest.tools;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class GNSSOneShoot {

    class Listener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            System.out.println ( "I got a location!" ) ;
        }
    }

    public GNSSOneShoot(Context context, LocationListener listener ) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            System.out.println("locationManager == null");
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            System.out.println("NO PERMISSION!");
            return ;
        }
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);

        /*
        //ComponentName rv = startService(new Intent(this, GPSService.class));

        Intent intent = new Intent(context, Listener.class);
        Connection connection = new Connection();
        System.out.println( "GNSS Service bind...");
        boolean rv = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);

        if ( rv )
            System.out.println ( "GNSS service is starting" ) ;
        else
            System.out.println ( "GNSS service is NOT starting" ) ;

         */
    }
}
