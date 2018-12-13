package de.bitsnarts.android.tools.renderingThread;

import android.graphics.Bitmap;

public class RenderingThread implements Runnable {

    private final RenderingThreadListener listener;
    private Bitmap bitmap;
    private PixelShader shader;
    private boolean restart;
    private int perc;

    public RenderingThread ( Bitmap bitmap, PixelShader shader, RenderingThreadListener listener ) {
        this.bitmap = bitmap ;
        this.shader = shader ;
        this.listener = listener ;
        new Thread ( this ).start() ;
    }

    public void startRendering (Bitmap bitmap, PixelShader shader ) {
        synchronized ( this ) {
            this.bitmap = bitmap ;
            this.shader = shader ;
            restart = true ;
        }
    }

    public void changeShader ( PixelShader shader ) {
        synchronized ( this ) {
            this.shader = shader;
            restart = true ;
        }
    }

    @Override
    public void run() {
        for (;;) {
            render () ;
        }
    }

    private void render() {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        for ( int y = 0 ; y < h ; y++ ) {
            synchronized ( this ) {
                for ( int x = 0 ; x < w ; x++ ) {
                    bitmap.setPixel(x, y, shader.getColorForPixel(x, y ) );
                }
                if ( restart )
                    return ;
            }
            int newPerc = (y + 1) * 100 / h;
            if ( newPerc != perc  ) {
                listener.renerdingProgressChanged(this, perc);
            }
        }
        synchronized ( this ) {
            listener.imageRendered( this );
        }
    }
}
