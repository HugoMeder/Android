package de.bitsnarts.android.apps.fractals;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import de.bitsnarts.android.tools.renderingThread.PixelShader;
import de.bitsnarts.android.tools.renderingThread.RenderingThread;
import de.bitsnarts.android.tools.renderingThread.RenderingThreadListener;

public class ProgressBar  extends View implements RenderingThreadListener {
    private int perc;
    private Paint redPaint;
    private Paint blackPaint;
    private Paint greenPaint;

    private Invalidaror invalidator = new Invalidaror () ;
    private boolean rendered;

    class Invalidaror implements Runnable {

        @Override
        public void run() {
            ProgressBar.this.invalidate();
        }
    }


    public ProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        redPaint = new Paint() ;
        redPaint.setStyle(Paint.Style.FILL );
        redPaint.setColor( 0xffff0000 );
        blackPaint = new Paint() ;
        blackPaint.setStyle(Paint.Style.FILL );
        blackPaint.setColor( 0xff000000 );
        greenPaint = new Paint() ;
        greenPaint.setStyle(Paint.Style.FILL );
        greenPaint.setColor( 0xff00ff00 );

    }

    @Override
    protected void onDraw(Canvas canvas) {
        boolean r ;
        int p ;
        synchronized ( this ) {
            p = perc ;
            r = rendered ;

        }
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if ( r ) {
            canvas.drawRect(0, 0, w, h, greenPaint );
        } else {
            int w1 = w*p/100 ;
            int w2 = w-w1 ;
            canvas.drawRect(0, 0, w1, h, redPaint );
            canvas.drawRect(w1, 0, w, h, blackPaint );
        }
    }

    @Override
    public void imageRendered(RenderingThread source, Bitmap bitmap, PixelShader shader ) {
        synchronized ( this ) {
            rendered = true;
        }
    }

    @Override
    public void renerdingProgressChanged(RenderingThread renderingThread, int perc) {
        synchronized ( this ) {
            this.perc = perc;
            rendered = false ;
        }
        post ( invalidator ) ;
    }
}