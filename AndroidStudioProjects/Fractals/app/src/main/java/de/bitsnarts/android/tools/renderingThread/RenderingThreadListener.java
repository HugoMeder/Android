package de.bitsnarts.android.tools.renderingThread;

import android.graphics.Bitmap;

public interface RenderingThreadListener {
    void imageRendered ( RenderingThread source, Bitmap bitmap, PixelShader shader ) ;
    void renerdingProgressChanged(RenderingThread renderingThread, int perc) ;
}
