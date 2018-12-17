package de.bitsnarts.android.tools.renderingThread;

import de.bitsnarts.shared.math.transforms.ConformalAffineTransform2D;

public interface PixelShader {
    int getColorForPixel ( int x, int y ) ;
    ConformalAffineTransform2D getTransform();
}
