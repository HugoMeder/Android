package de.bitsnarts.android.apps.fractals;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import de.bitsnarts.shared.math.transforms.ConformalAffineTransform2D ;

import de.bitsnarts.android.apps.fractals.graphics.Mandelbrot;
import de.bitsnarts.android.apps.fractals.graphics.SimpleFractalPainter;

public class MainView extends View {
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;
    private Paint canvasPaint;
    private Mandelbrot iter;
    private int[] colors;
    private boolean redraw = true ;
    private Paint textPaint;
    private Dragger dragger;
    private Matrix mat;

    public MainView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        iter = new Mandelbrot ( 255, 2.0 ) ;
        colors = new int[256] ;
        setColoring ( R.id.coloring_bw ) ;
        textPaint = new Paint();
        float ts = textPaint.getTextSize();
        textPaint.setTextSize( ts*3 );
        textPaint.setColor( 0xffff0000 );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap) ;
        w = canvasBitmap.getWidth();
        h = canvasBitmap.getHeight() ;
        double dw = 4.0 ;
        double dh = 4.0 ;
        double scale = dw/(w-1) ;
        // maps pixel coords to graphic coords
        ConformalAffineTransform2D tr = ConformalAffineTransform2D.createFGromAngleAndScale(0, scale, -w * scale / 2, -h * scale / 2);
        dragger = new Dragger ( tr ) ;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if ( redraw ) {
            //Logger.log.println ( "redraw" ) ;
            SimpleFractalPainter.paint ( canvasBitmap, dragger.getTransform (), colors, iter ) ;
            redraw = false ;
        }
        mat = dragger.getRelativeMatrix();
        if ( mat != null ) {
            //Logger.log.println ( "drawBitmap(mat)" ) ;
            canvas.drawBitmap(canvasBitmap, mat, canvasPaint);
        } else {
            //Logger.log.println ( "drawBitmap(null)" ) ;
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        }
        //dragger.drawState ( canvas ) ;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if ( redraw )
            return true ;
        boolean rv = dragger.onTouchEvent(event);
        if ( rv ) {
            if ( event.getAction() == MotionEvent.ACTION_UP )
                redraw = true ;
            invalidate();
        }
        return rv ;
    }

    private void initRedraw() {
        redraw = true ;
        invalidate () ;
    }

    public void setColoring(int id) {
        switch ( id ) {
            case R.id.coloring_bw:
                for ( int i = 0  ; i < 128 ; i++ ) {
                    colors[2*i+1] = (255*0x10101) | 0xff000000 ;
                    colors[2*i] = 0xff000000 ;
                }
                break ;
            case R.id.coloring_kernel:
                for ( int i = 0  ; i < 255 ; i++ ) {
                    colors[i] = 0xff000000 ;
                }
                colors[255] = 0xffffffff ;
                break ;
            case R.id.coloring_grayscale:
                for ( int i = 0  ; i < 256 ; i++ ) {
                    colors[i] = (i * 0x10101) | 0xff000000;
                }
                break ;
        }
		initRedraw() ;
    }

    Bitmap getBitmap () {
        return canvasBitmap ;
    }

}
