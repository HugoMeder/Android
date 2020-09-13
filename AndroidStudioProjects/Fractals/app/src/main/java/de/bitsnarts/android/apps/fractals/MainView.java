package de.bitsnarts.android.apps.fractals;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import de.bitsnarts.android.apps.fractals.graphics.FractalIterator;
import de.bitsnarts.android.apps.fractals.graphics.FractalPixelShader;
import de.bitsnarts.android.apps.fractals.graphics.Julia;
import de.bitsnarts.android.tools.logger.Logger;
import de.bitsnarts.android.tools.renderingThread.PixelShader;
import de.bitsnarts.android.tools.renderingThread.RenderingThread;
import de.bitsnarts.android.tools.renderingThread.RenderingThreadListener;
import de.bitsnarts.shared.math.transforms.ConformalAffineTransform2D ;

import de.bitsnarts.android.apps.fractals.graphics.Mandelbrot;

public class MainView extends View implements RenderingThreadListener {
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;
    private Paint canvasPaint;
    //private Mandelbrot iter;
    private Paint textPaint;
    private Dragger dragger;
    private Matrix mat;
    private FractalPixelShader fractalPixelShader;
    private RenderingThread renderingThread;
    private ProgressBar pb;
    private Bitmap randerBitmap;
    private FractalIterator iter;
    private ConformalAffineTransform2D tr;
    private Bitmap renderedBitmap;
    private Object renderSync = new Object () ;
    private Invalidaror invalidator = new Invalidaror () ;
    private ConformalAffineTransform2D tr0;
    private int[] colors;
    private int[] colors_bw;
    private int[] colors_kernel;
    private int[] colors_grayscale;

    class Invalidaror implements Runnable {

        @Override
        public void run() {
            MainView.this.invalidate();
        }
    }

    public MainView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    public void setProgressBar(ProgressBar bar) {
        this.pb = bar ;
    }

    private void setupDrawing() {
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        initColors () ;
        setColor ( R.id.coloring_grayscale ) ;
        textPaint = new Paint();
        float ts = textPaint.getTextSize();
        textPaint.setTextSize( ts*3 );
        textPaint.setColor( 0xffff0000 );
    }

    private void initColors() {
        colors_bw = new int[256] ;
        for ( int i = 0  ; i < 128 ; i++ ) {
            colors_bw[2*i+1] = (255*0x10101) | 0xff000000 ;
            colors_bw[2*i] = 0xff000000 ;
        }
        colors_kernel = new int[256] ;
        for ( int i = 0  ; i < 255 ; i++ ) {
            colors_kernel[i] = 0xff000000 ;
        }
        colors_kernel[255] = 0xffffffff ;
        colors_grayscale = new int[256] ;
        for ( int i = 0  ; i < 256 ; i++ ) {
            colors_grayscale[i] = (i * 0x10101) | 0xff000000;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        randerBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap) ;
        w = canvasBitmap.getWidth();
        h = canvasBitmap.getHeight() ;
        double dw = 4.0 ;
        double dh = 4.0 ;
        double scale = dw/(w-1) ;
        // maps pixel coords to graphic coords
        //iter = new Mandelbrot(255, 2.0);
        //x = 0.31156502751774207; y = 0.027430673607015097;
        //iter = new Julia(255, 2.0, 0.31156502751774207, 0.027430673607015097 );
        //x = -0.7855512517712042; y = -0.1408234738380438;
        //iter = new Julia(255, 2.0, -0.7855512517712042, -0.1408234738380438 );
        iter = new Mandelbrot(255, 2.0  );
        tr = ConformalAffineTransform2D.createFromAngleAndScale(0, scale, -w * scale / 2, -h * scale / 2);
        fractalPixelShader = new FractalPixelShader(iter, tr, colors);
        renderingThread = new RenderingThread( w, h, fractalPixelShader, this );
        dragger = new Dragger ( tr ) ;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized ( renderSync ) {
            if ( renderedBitmap == null )
                return ;
            if ( mat != null ) {
                canvas.drawBitmap(renderedBitmap, mat, canvasPaint);
            } else {
                canvas.drawBitmap(renderedBitmap, 0, 0, canvasPaint);
            }
        }
        //dragger.drawState( canvas );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean rv = dragger.onTouchEvent(event);
        if ( rv ) {
            synchronized ( renderSync ) {
                tr = dragger.getTransform() ;
                if ( tr != null ) {
                    fractalPixelShader = new FractalPixelShader(iter, tr, colors);
                    renderingThread.changeShader( fractalPixelShader );
                    makeMatrix () ;
                }
            }
            invalidate () ;
        }
        return true ;
    }

    private void makeMatrix() {
        if ( tr0 != null ) {
            ConformalAffineTransform2D t = tr.inverse().times(tr0);
            if ( mat == null ) {
                mat = new Matrix ( ) ;
            }
            float re = (float) t.real;
            float im = (float) t.imag ;
            float sx = (float) t.shiftX ;
            float sy = (float) t.shiftY ;
            float[] values = {
                    re, -im, sx,
                    im, re, sy,
                    0, 0, 1
            } ;
            mat.setValues( values );
        }
    }

    private void initRedraw() {
        invalidate () ;
    }

    public void setColor(int id) {
        switch (id) {
            case R.id.coloring_bw:
                colors = colors_bw;
                break;
            case R.id.coloring_kernel:
                colors = colors_kernel;
                break;
            case R.id.coloring_grayscale:
                colors = colors_grayscale;
                break;
            default:
                throw new Error ( "unknown color id" ) ;
        }
    }

    public void setColoring(int id) {
        setColor ( id ) ;
        synchronized ( renderSync ) {
            fractalPixelShader = new FractalPixelShader(iter, tr, colors);
            renderingThread.changeShader( fractalPixelShader );
        }
    }

    Bitmap getBitmap () {
        return renderedBitmap ;
    }

    @Override
    public void imageRendered(RenderingThread source, Bitmap bitmap, PixelShader shader  ) {
        pb.imageRendered(source, bitmap, shader );
        synchronized ( renderSync ) {
            renderedBitmap = bitmap ;
            mat = null ;
            tr0 = shader.getTransform () ;
            mat = null ;
            computeCentralCoordinates () ;
        }
        post ( invalidator ) ;
    }

    private void computeCentralCoordinates() {
        float x = canvasBitmap.getWidth()/ 2 ;
        float y = canvasBitmap.getHeight()/ 2 ;
        double[] in = new double[2];
        double[] out = new double[2];
        in[0] = x ;
        in[1] = y ;
        tr0.apply( in, out );
        //Logger.log().println ( "x = "+out[0]+"; y = "+out[1]+";" ) ;
    }

    @Override
    public void renerdingProgressChanged(RenderingThread renderingThread, int perc) {
        pb.renerdingProgressChanged(renderingThread, perc);
    }


}
