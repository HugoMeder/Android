package de.bitsnarts.android.apps.fractals;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import de.bitsnarts.android.apps.fractals.graphics.Mandelbrot;
import de.bitsnarts.android.apps.fractals.graphics.SimpleFractalPainter;

public class MainView extends View {
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;
    private Paint canvasPaint;
    private Mandelbrot iter;
    private int[] colors;
    private int cx, cy ;
    private boolean redraw = true ;
    private float downX, downY ;
    private double dx, dy ;

    public MainView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        iter = new Mandelbrot ( 255, 2.0 ) ;
        colors = new int[256] ;
		/*for ( int i = 0  ; i < 256 ; i++ ) {
			colors[i] = i*0x10101 ;
		}*/
        for ( int i = 0  ; i < 128 ; i++ ) {
            colors[2*i] = (255*0x10101) | 0xff000000 ;
            colors[2*i+1] = 0xff000000 ;
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap) ;
        //SimpleFractalPainter.paint ( drawCanvas, x0, y0, dx, dy, colors, iter ) ;
        //drawCanvas.drawRGB ( 0, 0, 0 ) ;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if ( redraw ) {
            int w = canvasBitmap.getWidth();;
            int h = canvasBitmap.getHeight() ;
            double dw = 4.0 ;
            double dh = 4.0 ;
            dx = dw/(w-1) ;
            dy = dx ;
            double x0 = -2.0-cx ;
            double y0 = -2.0-cy ;
            SimpleFractalPainter.paint ( canvasBitmap, x0, y0, dx, dy, colors, iter ) ;
            redraw = false ;
        }
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = touchX ;
                downY = touchY ;
                break;
            case MotionEvent.ACTION_MOVE:
                //drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                cx += (touchX-downX)*dx ;
                cy += (touchY-downY)*dy ;
                initRedraw () ;
                //drawCanvas.drawPath(drawPath, drawPaint);
                //drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    private void initRedraw() {
        redraw = true ;
        invalidate () ;
    }

}
