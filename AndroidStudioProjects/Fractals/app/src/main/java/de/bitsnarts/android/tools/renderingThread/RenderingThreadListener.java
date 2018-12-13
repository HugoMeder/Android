package de.bitsnarts.android.tools.renderingThread;

public interface RenderingThreadListener {
    void imageRendered ( RenderingThread source ) ;
    void renerdingProgressChanged(RenderingThread renderingThread, int perc) ;
}
