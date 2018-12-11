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
    private Paint textPaint;
    private int pointerCount;
    private boolean drawLine;
    private float px1, py1, px2, py2 ;

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
        //SimpleFractalPainter.paint ( drawCanvas, x0, y0, dx, dy, colors, iter ) ;
        //drawCanvas.drawRGB ( 0, 0, 0 ) ;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if ( redraw ) {
            int w = canvasBitmap.getWidth();
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
        drawText ( canvas ) ;
        if ( drawLine )
            drawLine ( canvas ) ;
    }

    private void drawLine(Canvas canvas) {
        canvas.drawLine( px1, py1, px2, py2, textPaint );
        float dx = (py1-py2)/2 ;
        float dy =-(px1-px2)/2 ;
        canvas.drawLine( px1+dx, py1+dy, px1-dx, py1-dy, textPaint );

    }

    private void drawText(Canvas canvas) {
        float ts = textPaint.getTextSize();
        float y = ts ;
        canvas.drawText( "pointerCount "+pointerCount, ts, y, textPaint );
        y += ts ;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        int pc = event.getPointerCount() ;
        if ( pc != pointerCount ) {
            pointerCount = pc ;
        }
        drawLine = false ;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = touchX ;
                downY = touchY ;
                break;
            case MotionEvent.ACTION_MOVE:
                if ( pointerCount == 2 ) {
                    px1 = event.getX( 0 ) ;
                    py1 = event.getY( 0 ) ;
                    px2 = event.getX( 1 ) ;
                    py2 = event.getY( 1 ) ;
                    drawLine = true ;
                }
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
