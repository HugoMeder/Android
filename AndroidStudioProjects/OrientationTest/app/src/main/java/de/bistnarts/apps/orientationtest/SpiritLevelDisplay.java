package de.bistnarts.apps.orientationtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;

import de.bistnarts.apps.orientationtest.tools.LowPassFilter;
import de.bistnarts.apps.orientationtest.tools.Quaternion;

public class SpiritLevelDisplay extends View implements AttachDetach {
    private Quaternion orientation;
    double[][]matrix = new double[3][3] ;
    float[]vec = new float[3] ;
    private Paint red;
    private Paint green;
    private Paint blue;
    private Paint[] rgb;
    private LowPassFilter axisFilter = new LowPassFilter ( 0.3f ) ;
    private Paint whiteFill;
    private Paint whiteStroke;

    public SpiritLevelDisplay(Context context) {
        super(context);
        init () ;
    }

    public SpiritLevelDisplay(Context context, AttributeSet attrs ) {
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
        whiteFill = new Paint();
        whiteFill.setARGB( 255, 255, 255, 255 );
        whiteFill.setStyle(Paint.Style.FILL);
        whiteStroke = new Paint();
        whiteStroke.setARGB( 255, 255, 255, 255 );
        whiteStroke.setStrokeWidth( sw );
        //whiteStroke.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.)) ;
        rgb = new Paint[3];
        rgb[0] = red ;
        rgb[1] = green ;
        rgb[2] = blue ;
    }

    public void setOrientation(float[] ori) {
        float[] values = axisFilter.filter(ori);
        orientation = new Quaternion ( values[3], values[0], values[1], values[2] ).normalize() ;
        orientation.getMatrix33( matrix );
        invalidate () ;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw( canvas );
        if ( matrix == null )
            return ;
        int w = getWidth();
        int h = getHeight();
        float r = w > h ? h : w;
        float cx = w / 2;
        float cy = h / 2;
        float scale = r / 3;

        double degreePerUnit = 1 ;
        double f = degreePerUnit / (Math.PI / 180.0);
        float dx = (float) (matrix[2][0] * scale * f);
        float dy = (float) (matrix[2][1] * scale * f);

        canvas.drawCircle( cx+dx, cy-dy, (float) (scale*0.2), whiteFill );

        for ( int i = 0 ; i < 2 ; i++ ) {
            for ( int j = 0 ; j < 3 ; j++ ) {
                vec[j] = (float)matrix[i][j] ;
            }
            drawAxis( vec, canvas, cx, cy, scale, whiteStroke ) ;
        }
    }

    private void drawAxis(float[] vector, Canvas canvas, float cx, float cy, float scale, Paint p) {
        canvas.drawLine( -vector[0]*scale+cx, cy+vector[1]*scale, vector[0]*scale+cx, cy-vector[1]*scale, p );
    }


    @Override
    public void attach() {
        axisFilter.reset();
    }

    @Override
    public void detach() {

    }

    @Override
    public boolean isAttached() {
        return false;
    }
}
