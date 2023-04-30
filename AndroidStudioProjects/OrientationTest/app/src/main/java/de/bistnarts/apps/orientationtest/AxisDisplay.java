package de.bistnarts.apps.orientationtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import de.bistnarts.apps.orientationtest.tools.GyrosopicIntegrator;
import de.bistnarts.apps.orientationtest.tools.LowPassFilter;
import de.bistnarts.apps.orientationtest.tools.Quaternion;

public class AxisDisplay extends View implements AttachDetach {
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

    public AxisDisplay(Context context) {
        super(context);
        init () ;
    }

    public AxisDisplay(Context context, AttributeSet attrs ) {
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
        white.setTextSize( 100 );
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
    public void setAngularVelocity(float[] values) {
        if ( orientation == null )
            return ;
        if ( axisFilter.getTimeSincveStart() < 10.0 )
            return ;
        if ( integrator == null ) {
            integrator = new GyrosopicIntegrator( orientation ) ;
        } else {
            integrator.nextSpin( values );
        }
        integrator.getState().getMatrix33( imatrix );
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw( canvas );
        int w = getWidth();
        int h = getHeight();

        //canvas.drawText( "text", 0, 100, white );

        int r = w > h ? h : w;
        int cx = w / 2;
        int cy = h / 2;
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


}
