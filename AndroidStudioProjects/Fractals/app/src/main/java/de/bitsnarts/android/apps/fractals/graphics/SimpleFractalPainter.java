package de.bitsnarts.android.apps.fractals.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.IOException;

import de.bitsnarts.shared.math.transforms.ConformalAffineTransform2D;

public class SimpleFractalPainter {

	public static void paint (Bitmap img, double x0, double y0, double dx, double dy, int[] colors, FractalIterator iter ) {
		int nx = img.getWidth() ;
		int ny = img.getHeight() ;
		for ( int x = 0 ; x < nx ; x++ ) {
			for ( int y = 0 ; y < ny ; y++ ) {
				img.setPixel(x, y, colors[iter.iteratationCount( x0+dx*x, y0+dy*y)] );
			}
		}
	}
	public static void paint (Canvas img, double x0, double y0, double dx, double dy, int[] colors, FractalIterator iter ) {
		int nx = img.getWidth() ;
		int ny = img.getHeight() ;
		int n = colors.length ;
		Paint paints[] = new Paint[n] ;
		for ( int i = 0 ; i < n ; i++ ) {
            Paint p = new Paint () ;
			p.setColor( colors[i] );
			paints[i] = p ;
		}
        Paint p = new Paint () ;
		p.setColor( 0xffff0000 );
		for ( int x = 0 ; x < nx ; x++ ) {
			for ( int y = 0 ; y < ny ; y++ ) {
				//Paint p_ = paints[iter.iteratationCount(x0 + dx * x, y0 + dy * y)];
				//img.drawPoint( x, y, p_ );
				img.drawPoint(x, y, paints[iter.iteratationCount( x0+dx*x, y0+dy*y)] );
			}
		}
	}


    public static void paint(Bitmap img, ConformalAffineTransform2D tr, int[] colors, Mandelbrot iter) {
		int nx = img.getWidth() ;
		int ny = img.getHeight() ;
		double[] in = new double[2] ;
		double[] out = new double[2] ;
		for ( int x = 0 ; x < nx ; x++ ) {
			for ( int y = 0 ; y < ny ; y++ ) {
				in[0] = x ;
				in[1] = y ;
				tr.apply( in, out );
				img.setPixel(x, y, colors[iter.iteratationCount( out[0], out[1] )] ) ;
			}
		}
    }
}
