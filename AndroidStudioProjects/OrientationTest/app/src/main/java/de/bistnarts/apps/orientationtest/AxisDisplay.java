package de.bistnarts.apps.orientationtest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import de.bistnarts.apps.orientationtest.tools.GNSSOneShoot;
import de.bistnarts.apps.orientationtest.tools.Globals;
import de.bistnarts.apps.orientationtest.tools.GyrosopicIntegrator;
import de.bistnarts.apps.orientationtest.tools.LowPassFilter;
import de.bistnarts.apps.orientationtest.tools.PropertyAccess;
import de.bistnarts.apps.orientationtest.tools.Quaternion;
import de.bistnarts.apps.orientationtest.tools.TextDrawer;

public class AxisDisplay extends View implements AttachDetach, View.OnClickListener, View.OnLongClickListener, LocationListener {
    private Quaternion orientation;
    double[][]matrix = new double[3][3] ;
    float[]vec = new float[3] ;
    private Paint red;
    private Paint green;
    private Paint blue;
    private Paint[] rgb;
    private LowPassFilter axisFilter = new LowPassFilter ( 0.3f ) ;
    private LowPassFilter magneticFilter = new LowPassFilter ( 0.3f ) ;
    private float[] magneticField;
    private Paint white;
    private float[] accelleration;
    GyrosopicIntegrator integrator ;
    double[][] imatrix = new double[3][3] ;
    
    private Paint grey;
    private Paint ired;
    private Paint igreen;
    private Paint iblue;
    private Paint[] irgb;
    private TextDrawer td;
    private int clickCount;
    private GNSSOneShoot gnss;
    private GeomagneticField geomRef;
    private boolean zeroAngularMomentum;
    private Globals globals;
    private double locLon;
    private double locLat;
    private double locAlt;
    private long locTime;


    public AxisDisplay(Context context) {
        super(context);
        init () ;
    }

    public AxisDisplay(Context context, AttributeSet attrs ) {
        super(context, attrs );
        init () ;
    }

    void setGlobals ( Globals globals ) {
        this.globals = globals ;
        loadGeomRef();
    }

    void openUrl ( String download_link) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(download_link));
        globals.getActivity().startActivity(myIntent);
    }

    private void init() {

        int sw = 10 ;
        red = new Paint();
        red.setARGB( 255, 255, 0, 0 );
        red.setStrokeWidth( sw );
        green = new Paint();
        green.setARGB( 255, 0, 255, 0 );
        green.setStrokeWidth( sw );
        blue = new Paint();
        blue.setARGB( 255, 100, 100, 255 );
        blue.setStrokeWidth( sw );
        rgb = new Paint[3];
        rgb[0] = red ;
        rgb[1] = green ;
        rgb[2] = blue ;

        white = new Paint();
        white.setARGB( 255, 255, 255, 255 );
        white.setStrokeWidth( sw );
        white.setTextSize( 50 );
        white.setTypeface( Typeface.MONOSPACE ) ;
        white.setTextScaleX( 0.7f );
        td = new TextDrawer(white);

        grey = new Paint();
        grey.setARGB( 255, 100, 100, 100 );
        grey.setStrokeWidth( sw );

        ired = new Paint();
        ired.setARGB( 255, 255, 0, 0 );
        ired.setStrokeWidth( sw/2 );
        igreen = new Paint();
        igreen.setARGB( 255, 0, 255, 0 );
        igreen.setStrokeWidth( sw/2 );
        iblue = new Paint();
        iblue.setARGB( 255, 100, 100, 255 );
        iblue.setStrokeWidth( sw/2 );

        irgb = new Paint[3];
        irgb[0] = ired ;
        irgb[1] = igreen ;
        irgb[2] = iblue ;

        this.setClickable( true ) ;
        setOnClickListener( this );
        setOnLongClickListener( this );

        gnss = new GNSSOneShoot(this.getContext(), this);
    }

    public void setOrientation(float[] ori) {
        float[] values = axisFilter.filter(ori);
        orientation = new Quaternion ( values[3], values[0], values[1], values[2] ).normalize() ;
        orientation.getMatrix33( matrix );
        invalidate () ;
    }
    public void setMagneticField(float[] values) {
        magneticField = magneticFilter.filter( values ) ;
        invalidate();
    }

    public void setAccelleration(float[] values) {
        accelleration = values ;
    }
    public void setAngularVelocity(float[] values, long timestamp ) {
        float[] v = values;
        double vel = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        zeroAngularMomentum = vel==0 ;
        if ( orientation == null )
            return ;
        if ( axisFilter.getTimeSincveStart() < 3.0 )
            return ;
        if ( integrator == null ) {
            integrator = new GyrosopicIntegrator( orientation, timestamp ) ;
        } else {
            integrator.nextSpin( values, timestamp );
        }
        integrator.getState().getMatrix33( imatrix );
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw( canvas );
        int w = getWidth();
        int h = getHeight();

        //canvas.drawText( "text", 0, 100, white );

        Vector<String> txt = new Vector<String>();
        if ( geomRef != null ) {
            txt.add( String.format( Locale.ENGLISH,  "Referenzwerte für Position" ) );
            txt.add ( String.format( Locale.ENGLISH,  "\tlat %7.3f\u00B0 lon %7.3f\u00B0", locLat, locLon ) ) ;
            txt.add( String.format( Locale.ENGLISH,  "\tdec %7.1f\u00B0", geomRef.getDeclination() ) ) ;
            txt.add( String.format( Locale.ENGLISH,  "\tinc %7.1f\u00B0", geomRef.getInclination() ) ) ;
            txt.add(String.format( Locale.ENGLISH,  "\tB   %7.1f \u03BCT", geomRef.getFieldStrength()/1000.0 ) ) ;
            //double mx = dot ( matrix[0], magneticField ) ;
         }

        if ( magneticField != null && orientation != null ) {
            float[] v = magneticField;
            double bLen = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
            double Bx = dot(matrix[0], magneticField);
            double By = dot(matrix[1], magneticField);
            double Bz = dot(matrix[2], magneticField);
            double Bplane = Math.sqrt(Bx * Bx + Bz * Bz);
            double incl = Math.atan2(Bplane, Math.abs(By));
            double decl = Math.atan2(Bx, By);
            txt.add ( "Magnetfeld" ) ;
            if ( false ) {
                txt.add( String.format( Locale.ENGLISH, "\tBx   %6.1f \u03BCT", Bx ) ) ;
                txt.add( String.format( Locale.ENGLISH, "\tBy   %6.1f \u03BCT", By ) ) ;
                txt.add( String.format( Locale.ENGLISH, "\tBz   %6.1f \u03BCT", Bz ) ) ;
            }

            txt.add( String.format( Locale.ENGLISH, "\tinc  %6.1f\u00B0", incl*180.0/Math.PI ) ) ;
            txt.add( String.format( Locale.ENGLISH, "\tdec  %6.1f\u00B0", decl*180.0/Math.PI ) ) ;
            txt.add( String.format( Locale.ENGLISH, "\tB    %6.1f \u03BCT", bLen ) ) ;

        }

        if ( integrator!=null  && orientation!= null ) {
            Quaternion delta = orientation.times(integrator.getState().inverse());
            double i = delta.getI();
            double j = delta.getJ();
            double k = delta.getK();
            double re = delta.getR() ;
            double s = Math.sqrt(i * i + j * j + k * k);
            double c = Math.abs(re);
            double phi = Math.atan2(s, c)*2.0;
            txt.add( "Abweichung "+String.format( Locale.ENGLISH, "%7.2f\u00B0", (phi*180.0/Math.PI))+" seit "+String.format( Locale.ENGLISH,  "%4.2f",(integrator.getSecsAfterStart())/60)+" min") ;
            if ( zeroAngularMomentum ) {
                txt.add ( "\tzero angular velocity!") ;
            } else {
                txt.add ( "\tnonzero angular velocity");
            }
        }

        td.setText(txt);
        td.getFullTextHeight() ;
        int txtx = 100;
        int txty = 100;
        h = h - (int)td.getFullTextHeight()-txty ;
        int r = w > h ? h : w;
        int cx = w / 2;
        int cy = (int)td.getFullTextHeight()+txty+h / 2;
        int scale = r / 3;

        if ( matrix != null ) {
            for ( int i = 0 ; i < 3 ; i++ ) {
                for ( int j = 0 ; j < 3 ; j++ ) {
                    vec[j] = (float)matrix[i][j] ;
                }
                drawVector ( vec, canvas, cx, cy, scale, rgb[i] ) ;
            }
        }
        if ( integrator != null ) {
            for ( int i = 0 ; i < 3 ; i++ ) {
                for ( int j = 0 ; j < 3 ; j++ ) {
                    vec[j] = (float)imatrix[i][j] ;
                }
                drawVector ( vec, canvas, cx, cy, scale, irgb[i] ) ;
            }

        }
        if ( magneticField != null ) {
            drawVector( magneticField, canvas, cx, cy, scale/30, white);
        }
        if ( accelleration != null )
            drawVector( accelleration, canvas, cx, cy, scale/10, grey );

        if ( !txt.isEmpty() ) {
            //txt.add( "und noch eine Zeile") ;
            td.setText( txt );
            td.drawOnto( canvas, 100, 100 );
        }

    }

    private double dot(double[] a, float[] b) {
        double rv = 0.0;
        for ( int i = 0 ; i < 3 ; i++ ){
            rv += a[i]*b[i] ;
        }
        return rv ;
    }

    private void drawVector(float[] vector, Canvas canvas, float cx, float cy, float scale, Paint p) {
        canvas.drawLine( cx, cy, vector[0]*scale+cx, cy-vector[1]*scale, p );
    }


    @Override
    public void attach() {
        axisFilter.reset () ;
        magneticFilter.reset();
        if ( integrator != null )
            integrator = null ;

    }

    @Override
    public void detach() {

    }

    @Override
    public boolean isAttached() {
        return false;
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLongClick(View v) {
        clickCount++ ;
        if ( integrator != null ) {
            integrator.reset( orientation, 0 );
        }
        return false ;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Date date = new Date();
        locLon = location.getLongitude() ;
        locLat = location.getLatitude() ;
        locAlt = location.getAltitude() ;
        locTime = location.getTime() ;
        createGeomRef () ;
        storeGeomRef ( location.getLongitude(), location.getLatitude(), location.getAltitude(), date.getTime() ) ;
    }

    private void createGeomRef() {
        this.geomRef = new GeomagneticField((float) locLat, (float) locLon, (float) locAlt, locTime ) ;
    }

    private void storeGeomRef(double longitude, double latitude, double altitude, long time) {
        PropertyAccess pa = globals.getPropertyAccess();
        Properties props = new Properties();
        props.setProperty( "loc.lon", ""+longitude ) ;
        props.setProperty( "loc.lat", ""+latitude ) ;
        props.setProperty( "loc.alt", ""+altitude ) ;
        props.setProperty( "loc.timeMs", ""+time ) ;
        pa.setProperties( props );
    }

    private void loadGeomRef () {
        PropertyAccess pa = globals.getPropertyAccess();
        String lonStr = pa.getProperty("loc.lon");
        if ( lonStr == null ) {
            return ;
        }
        locLon = Double.parseDouble(lonStr);
        locLat = Double.parseDouble(pa.getProperty("loc.lat"));
        locAlt = Double.parseDouble(pa.getProperty("loc.alt"));
        locTime = Long.parseLong(pa.getProperty("loc.timeMs"));
        createGeomRef();
    }
}
