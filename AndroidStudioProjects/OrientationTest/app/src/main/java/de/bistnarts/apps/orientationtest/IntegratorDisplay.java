package de.bistnarts.apps.orientationtest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.lights.Light;
import android.hardware.lights.LightsManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import de.bistnarts.apps.orientationtest.tools.GyrosopicAxisListener;
import de.bistnarts.apps.orientationtest.tools.GyrosopicIntegrator;
import de.bistnarts.apps.orientationtest.tools.PathIntegrator;
import de.bistnarts.apps.orientationtest.tools.Quaternion;
import de.bistnarts.apps.orientationtest.tools.TextDrawer;

public class IntegratorDisplay extends View implements AttachDetach, View.OnLongClickListener, GyrosopicAxisListener {
    private Quaternion orientation = new Quaternion(1, 0, 0, 0);
    private double[][] oMat = new double[3][3] ;
    double[][]matrix = new double[3][3] ;
    float[]vec = new float[3] ;
    private Paint red;
    private Paint green;
    private Paint blue;
    private Paint[] rgb;
    private Paint white;
    private float[] accelleration;
    PathIntegrator integrator ;
    double[][] imatrix = new double[3][3] ;

    private Paint grey;
    private Paint ired;
    private Paint igreen;
    private Paint iblue;
    private Paint[] irgb;
    private TextDrawer td;
    private int numAccelerationEvents;
    private int numAngularEvents;
    private long startTime;
    //private Camera cam;
    private boolean LEDon;
    private GyrosopicIntegrator.AxisMeasurementResult axisMeasurement;

    public IntegratorDisplay(Context context) {
        super(context);
        init () ;
    }

    public IntegratorDisplay(Context context, AttributeSet attrs ) {
        super(context, attrs );
        init () ;
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
        setOnLongClickListener( this );

        this.orientation.getMatrix33( oMat );
    }

    public void setAccelleration(float[] values, long timestamp) {

        accelleration = values ;
        numAccelerationEvents++ ;
        if ( integrator != null ) {
            integrator.setAcceleration(values, timestamp );
        }
        invalidate();
    }
    public void setAngularVelocity(float[] values, long timestamp) {
        numAngularEvents++ ;
        float[] v = values;
        double vel = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        boolean zeroAngularMomentum = vel == 0;
        if ( integrator == null ) {
            integrator = new PathIntegrator( orientation, null, timestamp ) ;
        } else {
            integrator.setAngularVelocity( values, timestamp );
        }
        integrator.getGyrosopicIntegrator().getState().getMatrix33( imatrix );
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw( canvas );
        int w = getWidth();
        int h = getHeight();

        //canvas.drawText( "text", 0, 100, white );

        Vector<String> txt = new Vector<String>();

        if ( integrator!=null  && orientation!= null ) {
            Quaternion delta = orientation.times(integrator.getGyrosopicIntegrator().getState().inverse());
            double i = delta.getI();
            double j = delta.getJ();
            double k = delta.getK();
            double re = delta.getR() ;
            double s = Math.sqrt(i * i + j * j + k * k);
            double c = Math.abs(re);
            double phi = Math.atan2(s, c)*2.0;
            txt.add( "Abweichung "+String.format( Locale.ENGLISH, "%7.2f\u00B0", (phi*180.0/Math.PI))+" seit "+String.format( Locale.ENGLISH,  "%4.2f",(integrator.getGyrosopicIntegrator().getSecsAfterStart())/60)+" min") ;

        }

        if ( integrator != null ) {
            if ( true ) {
                double[] pos = integrator.getPositionWS();
                txt.add( "Position" ) ;
                for ( int i = 0  ; i < 3 ; i++ ) {
                    txt.add( "\t["+i+String.format( Locale.ENGLISH, "] %5.1f m", pos[i]) ) ;
                }
            }
            if ( false ) {
                double[] q = integrator.getGyrosopicIntegrator().getState().asDoubleArray();
                double imAbs = Math.sqrt(q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
                double reAbs = Math.abs(q[0]);
                double phi = Math.atan2(imAbs, reAbs) * 2.0;
                txt.add ( String.format( Locale.ENGLISH, "Winkel %7.3f", (phi*180.0/Math.PI)) ) ;
            }
        }
        if ( true ) {
            long now = System.currentTimeMillis();
            double secs = (now - startTime) / 1000.0;
            txt.add( "event statistics" ) ;
            txt.add( "\tacceleration" ) ;
            txt.add( "\t\t"+String.format( Locale.ENGLISH, "freq %7.3f Hertz", (numAccelerationEvents/secs))) ;
            txt.add( "\tomega" ) ;
            txt.add( "\t\t"+String.format( Locale.ENGLISH, "freq %7.3f Hertz", (numAngularEvents/secs))) ;
        }
        if ( axisMeasurement != null ) {
            txt.add( "axis measurement" ) ;
            String strxyz = "xyz" ;
            double[] axis = axisMeasurement.getAxis();
            for ( int i = 0 ; i < 3 ; i++ ) {
                txt.add( "\taxis["+strxyz.charAt(i)+"] "+String.format(Locale.ENGLISH, "%10.3f", axis[i])) ;
            }
            txt.add( String.format( Locale.ENGLISH, "\terror %10.5f", axisMeasurement.getError()) ) ;
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


        if ( oMat != null ) {
            for ( int i = 0 ; i < 3 ; i++ ) {
                for ( int j = 0 ; j < 3 ; j++ ) {
                    vec[j] = (float)oMat[i][j] ;
                }
                drawVector ( vec, canvas, cx, cy, scale, irgb[i] ) ;
            }
        }
        if ( integrator != null ) {
            for ( int i = 0 ; i < 3 ; i++ ) {
                for ( int j = 0 ; j < 3 ; j++ ) {
                    vec[j] = (float)imatrix[i][j] ;
                }
                drawVector ( vec, canvas, cx, cy, scale, rgb[i] ) ;
            }
            //double[] pos = integrator.getPositionWS();
            //drawVector ( vec, canvas, cx, cy, scale/10, this.grey ) ;

        }
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
        if ( integrator != null )
            integrator = null ;
        resetCounters () ;
   }

    private void resetCounters() {
        numAngularEvents = 0 ;
        numAccelerationEvents = 0 ;
        startTime = System.currentTimeMillis() ;
    }

    @Override
    public void detach() {

    }

    @Override
    public boolean isAttached() {
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        if ( integrator != null ) {
            integrator.reset( orientation, null, null, 0, this );
        }
        resetCounters();
        toggle_LED() ;
        return false ;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void toggle_LED2() {
        //getContext().getSystemService( android.content.Context.LIGHTS_SERVICE ) ;
        //LightsManager lm = new LightsManager(getContext());
        //List<Light> lights = lm.getLights();
        System.out.println( "got lights!");
    }

    private void toggle_LED() {
        /*
        if (cam == null) {
            cam = openCamera();
        }
        if (cam == null)
            return;
        LEDon = !LEDon;
        Object rv;
        if (LEDon) {
            Camera.Parameters param = cam.getParameters();
            param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(param);
            cam.startPreview();
        } else {
            Camera.Parameters param = cam.getParameters();
            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            cam.setParameters(param);
            cam.stopPreview();
        }*/
    }

    private Camera openCamera() {
        Camera c=null;
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            c = Camera.open();
        }
        return c ;
    }

    @Override
    public void axisReady(GyrosopicIntegrator.AxisMeasurementResult result) {
        this.axisMeasurement = result ;
        invalidate();
    }


    /*public void setAccelerationAndAngularMomentum(float[] acceleration, float[] angularVelocity, long timestamp) {
        setAccelleration( acceleration );
        setAngularVelocity( angularVelocity, timestamp );
    }*/
}
