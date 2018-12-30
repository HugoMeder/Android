package com.example.hugomeder.gpstest;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.Date;
import java.util.List;

import de.bitsnarts.android.gps.service.GPSService;
import de.bitsnarts.android.gps.service.IGPSService;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView textView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private File filesDir;
    private static final int maxNUmConstellations = 7 ;
    private int satelliteCounts[] = new int[maxNUmConstellations];
    private GnssStatus.Callback gnssStalusListener;
    private boolean binder;
    private Connection connection;

    class Connection implements ServiceConnection {

        private IGPSService service;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GPSService.MyBinder b = (GPSService.MyBinder) service;
            this.service = b.getService() ;
            this.service.requestCallbacks();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        public IGPSService getService () {
            return service ;
        }
    } ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //ComponentName rv = startService(new Intent(this, GPSService.class));

        Intent intent = new Intent(this, GPSService.class);
        connection = new Connection () ;
        boolean rv = bindService(intent, connection, Context.BIND_AUTO_CREATE);;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if ( location == null )
                    textView.append( "?");
                else {
                    Date d = new Date ( location.getTime() ) ;
                    StringBuffer buf = new StringBuffer () ;
                    for ( int i = 0 ; i < maxNUmConstellations ; i++ ) {
                        buf.append( " "+satelliteCounts[i] ) ;
                    }
                    textView.setText( "\n"+connection.getService().getNumLogsDone()+" geloggt"+
                            "\n"+d +
                            "\nSatelliten "+buf+
                            "\nHöhe "+location.getAltitude()+
                            "\nBreite "+location.getLatitude()+
                            "\nLänge "+ location.getLongitude()+
                            "\nGenauigkeit " + location.getAccuracy()
                    ) ;
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
                Intent intent = new Intent (Settings.ACTION_LOCATION_SOURCE_SETTINGS) ;
                startActivity(intent);
            }
        };

        gnssStalusListener = new GnssStatus.Callback(){

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {

                int n = status.getSatelliteCount();
                for ( int i = 0 ; i < 6 ; i++ ) {
                    satelliteCounts[i] = 0 ;
                }
                for ( int i = 0 ; i < n ; i++ ) {
                    satelliteCounts[status.getConstellationType(i)]++ ;
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            boolean no_perm1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
            boolean no_perm2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
            if ( no_perm1 && no_perm2 ) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.INTERNET}
                        , 10);
                return;
            } else {
                configureButton();
            }
            boolean no_perm3 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;

        }else{
            configureButton () ;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 10:
                if ((grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED))
                    configureButton();
                else
                    System.out.println ( "No permission?" ) ;
                return;
        }
    }

    private void configureButton() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    List<String> providers = locationManager.getAllProviders();

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, locationListener);
                    locationManager.registerGnssStatusCallback ( gnssStalusListener ) ;
                    Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);;
                    textView.append( "\ndata...");
                    locationListener.onLocationChanged( loc );
                } catch ( Throwable th ) {
                    th.printStackTrace();
                }
            }
        });
    }
    }

