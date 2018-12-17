package de.bitsnarts.android.tools.renderingThread;

import android.graphics.Bitmap;

public class RenderingThread implements Runnable {

    private final RenderingThreadListener listener;
    private Bitmap lastRenderedBitmap;
    private Bitmap renderingBitmap;
    private PixelShader shader;
    private boolean restart;
    private boolean finished;
    private boolean run ;
    private int perc;
    private long lastMod;

    public RenderingThread ( int width, int height, PixelShader shader, RenderingThreadListener listener ) {
        this.renderingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) ;
        this.lastRenderedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) ;
        this.shader = shader ;
        this.listener = listener ;
        this.run = true ;
        Thread thr = new Thread(this) ;
        thr.setName ( "RenderingThread" ) ;
        thr.setPriority( Thread.MIN_PRIORITY );
        lastMod = System.currentTimeMillis() ;
        thr.start();
    }

    /*public void startRendering (Bitmap bitmap, PixelShader shader ) {
        synchronized ( this ) {
            this.renderingBitmap = bitmap ;
            this.shader = shader ;
            restart = true ;
            finished = false ;
            this.notifyAll();
        }
    }*/

    public void changeShader ( PixelShader shader ) {
        synchronized ( this ) {
            this.shader = shader;
            restart = true ;
            finished = false ;
            lastMod = System.currentTimeMillis() ;
            this.notifyAll();
        }
    }

    @Override
    public void run() {
        for (;;) {
            synchronized ( this ) {
                while ( finished ) {
                    try {
                        this.wait () ;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if ( restart ) {
                for (;;) {
                    long delta ;
                    synchronized ( this ) {
                        delta = System.currentTimeMillis() - lastMod;
                        listener.renerdingProgressChanged(this, 0);
                    }
                    if ( delta < 100 ) {
                        try {
                            Thread.sleep( 10 );
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        break ;
                    }
                }
            }
            restart = false ;
            render () ;
        }
    }

    private void render() {
        perc = 0 ;
        listener.renerdingProgressChanged(this, perc);
        int w = renderingBitmap.getWidth();
        int h = renderingBitmap.getHeight();
        for ( int y = 0 ; y < h ; y++ ) {
            synchronized ( this ) {
                for ( int x = 0 ; x < w ; x++ ) {
                    renderingBitmap.setPixel(x, y, shader.getColorForPixel(x, y ) );
                }
                if ( restart )
                    return ;
            }
            int newPerc = (y + 1) * 100 / h;
            //newPerc = newPerc % 10 ;
            if ( newPerc != perc  ) {
                perc = newPerc ;
                listener.renerdingProgressChanged(this, perc);
                //Logger.log().println ( ""+perc+"%") ;
            }
        }
        Bitmap bm = null ;
        PixelShader shdr = null ;

        synchronized ( this ) {
            if ( restart )
                return ;
            finished = true ;
            Bitmap tmp = renderingBitmap;;
            renderingBitmap = lastRenderedBitmap ;
            lastRenderedBitmap = tmp ;
            bm = lastRenderedBitmap ;
            shdr = shader ;
        }
        listener.imageRendered( this, bm, shdr );
    }
}
