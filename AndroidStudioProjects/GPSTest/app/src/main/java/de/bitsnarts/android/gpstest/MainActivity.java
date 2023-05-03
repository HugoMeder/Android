package de.bitsnarts.android.gpstest;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
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
import android.text.TextPaint;
import android.widget.TextView;

//import com.example.hugomeder.gpstest.R;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.bitsnarts.android.gps.service.GPSService;
import de.bitsnarts.android.gps.service.IGPSService;
import de.bitsnarts.android.gps.service.PGMReader;
import de.bitsnarts.geoid.Geoid;
import de.bitsnarts.geoid.GeoidInterface ;

public class MainActivity extends AppCompatActivity {

    //private Button button;
    private TextView textView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private File filesDir;
    private static final int maxNumConstellations = 8 ;
    private int satelliteCounts[] = new int[maxNumConstellations];
    private GnssStatus.Callback gnssStalusListener;
    private boolean binder;
    private Connection connection;
    private String[] satelliteType = {
            "?", "GPS", "SBAS", "GLO", "QZ", "BEI", "GAL", "IRN"
    } ;

    private static PGMReader geoid ;
    private static GeoidInterface geoid2 = new Geoid() ;
    private static VelocityOnSphere vos = new VelocityOnSphere() ;
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
        //loadGeoid () ;
        setContentView(R.layout.activity_main);
        //button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        textView.setTextScaleX( 0.7f ) ;
        textView.setTypeface( Typeface.MONOSPACE);
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //ComponentName rv = startService(new Intent(this, GPSService.class));

        Intent intent = new Intent(this, GPSService.class);
        connection = new Connection () ;
        boolean rv = bindService(intent, connection, Context.BIND_AUTO_CREATE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if ( location == null )
                    textView.append( "?");
                else
                    textView.setText( getTextForLocation ( location ) ) ;
            }

            String getTextForLocation ( Location location ) {
                if ( true )
                    return getOldTextForLocation (location ) ;
                else
                    return getNewTextForLocation (location ) ;
            }

    private String getNewTextForLocation(Location location) {

        Date d = new Date ( location.getTime() ) ;
        StringBuffer buf = new StringBuffer () ;
        for (int i = 0; i < maxNumConstellations; i++ ) {
            int sc = satelliteCounts[i] ;
            if ( sc != 0 ) {
                buf.append(" " + satelliteCounts[i]) ;
                buf.append ( satelliteType[i] ) ;
            }
        }
        double delta = 0 ;
        double g = 0 ;
        double ac = geoid2.getAthmosphericCorrectionAcceleration() ;
        geoid2.setPosition( location.getLongitude(), location.getLatitude() );
        vos.setPosition ( d, location.getLongitude(), location.getLatitude() ) ;
        delta = geoid2.getUndulation() ;
        g = geoid2.getGravity() ;
        double hog_m = location.getAltitude() - delta;
        double hog_ft = hog_m / 0.3048;
        String rv = "\n" + connection.getService().getNumLogsDone() + " geloggt" +
                    "\n" + d +
                    "\nSats " + buf +
                    "\nHöhe über Ref-Ell." + location.getAltitude() + " m" +
                    "\nGeoid Höhe " + String.format("%3.4f", delta) + " m" +
                    "\nHöhe über Geoid " + String.format("%10.1f m, %10.1f ft", hog_m, hog_ft) +
                    "\nGravitation " + String.format("%3.8f", g) + " m/(s^2)" +
                    "\nGravitation ac " + String.format("%3.8f", ac) + " m/(s^2)" +
                    "\nLänge " + String.format("%3.5f", location.getLongitude()) + " \u00B0" +
                    "\nBreite " + String.format("%3.5f", location.getLatitude()) + " \u00B0" +
                    "\nGenauigkeit " + String.format("%10.1f", location.getAccuracy()) + " m" ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rv += "\nGenauigkeit (Höhe)" + String.format("%10.1f", location.getVerticalAccuracyMeters()) + " m";
        }
        return rv ;
    }

    private String getOldTextForLocation(Location location) {

        Date d = new Date ( location.getTime() ) ;
        StringBuffer buf = new StringBuffer () ;
        for (int i = 0; i < maxNumConstellations; i++ ) {
            int sc = satelliteCounts[i] ;
            if ( sc != 0 ) {
                buf.append(" " + satelliteCounts[i]) ;
                buf.append ( satelliteType[i] ) ;
            }
        }
        double delta = 0 ;
        double g = 0 ;
        double ac = geoid2.getAthmosphericCorrectionAcceleration() ;
        geoid2.setPosition( location.getLongitude(), location.getLatitude() );
        vos.setPosition ( d, location.getLongitude(), location.getLatitude() ) ;
        delta = geoid2.getUndulation() ;
        g = geoid2.getGravity() ;
        double hog_m = location.getAltitude() - delta;
        double hog_ft = hog_m / 0.3048;;
        String rv = "\n" + connection.getService().getNumLogsDone() + " geloggt" +
                "\n" + d +
                "\nSats " + buf +
                "\nHöhe über Ref-Ell." + location.getAltitude() + " m" +
                "\nGeoid Höhe " + String.format(Locale.ENGLISH,"%3.4f", delta) + " m" +
                "\nHöhe über Geoid " + String.format(Locale.ENGLISH,"%10.1f m, %10.1f ft", hog_m, hog_ft) +
                "\nGravitation " + String.format(Locale.ENGLISH,"%3.8f", g) + " m/(s^2)" +
                "\nGravitation ac " + String.format(Locale.ENGLISH,"%3.8f", ac) + " m/(s^2)" +
                "\nLänge " + String.format(Locale.ENGLISH,"%3.5f", location.getLongitude()) + " \u00B0" +
                "\nBreite " + String.format(Locale.ENGLISH,"%3.5f", location.getLatitude()) + " \u00B0" +
                "\nGenauigkeit " + String.format(Locale.ENGLISH,"%10.1f", location.getAccuracy()) + " m" ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rv += "\nGenauigkeit (Höhe)" + String.format(Locale.ENGLISH,"%10.1f", location.getVerticalAccuracyMeters()) + " m";
        }
        if ( vos.hasVel ) {
            double vN = vos.velNord * 3600.0 / 1000.0;
            double vE = vos.velEast *3600.0 / 1000.0;
            double v = Math.sqrt(vN * vN + vE * vE);
            rv += "\nGeschwindigkeit "+String.format(Locale.ENGLISH, "%3.1f km/h", v ) ;
            if ( v != 0.0 ) {
                double phiDeg = Math.atan2( vE, vN )*180/Math.PI ;
                rv += ", "+String.format(Locale.ENGLISH, "%3.1f", phiDeg)+((char)0x00B0) ;
            }
        }
        return rv ;
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
                for ( int i = 0 ; i < maxNumConstellations ; i++ ) {
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

    private void loadGeoid() {
        if ( geoid != null )
            return ;
        File dir = this.getFilesDir() ;
        File file = new File ( dir, "egm2008-5.pgm" ) ;
        try {
            geoid = new PGMReader ( file ) ;
        } catch (IOException e) {
            e.printStackTrace();
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
        /*
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
        })
        */
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
        ;
    }
    }

