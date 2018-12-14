package de.bitsnarts.android.apps.fractals.graphics;

import de.bitsnarts.android.tools.renderingThread.PixelShader;
import de.bitsnarts.shared.math.transforms.ConformalAffineTransform2D;

public class FractalPixelShader implements PixelShader {

    private final FractalIterator iterator;
    private final ConformalAffineTransform2D tr;
    private final int[] colors;
    private double[] in = new double[2] ;
    private double[] out = new double[2] ;

    public FractalPixelShader (FractalIterator iterator, ConformalAffineTransform2D tr, int[] colors ) {
        this.iterator = iterator ;
        this.tr = tr ;
        this.colors = colors ;
    }

    @Override
    public int getColorForPixel(int x, int y) {
        in[0] = x ;
        in[1] = y ;
        tr.apply( in, out ) ;
        return colors[iterator.iteratationCount(out[0], out[1])];
    }

}
