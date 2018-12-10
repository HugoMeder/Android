package de.bitsnarts.android.apps.fractals.graphics.unittest;

import android.graphics.Bitmap;

import de.bitsnarts.android.apps.fractals.graphics.Mandelbrot;
import de.bitsnarts.android.apps.fractals.graphics.SimpleFractalPainter;

public class MandelbrotOnBitmap {

    public static void main ( String[] args ) {
        Mandelbrot iter = new Mandelbrot ( 255, 2.0 ) ;
        int w = 1024 ;
        int h = 1024 ;
        Bitmap img = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);;
        double dw = 4.0 ;
        double dh = 4.0 ;
        double dx = dw/(w-1) ;
        double dy = dh/(h-1) ;
        double x0 = -2.0 ;
        double y0 = -2.0 ;
        int[] colors = new int[256] ;
		/*for ( int i = 0  ; i < 256 ; i++ ) {
			colors[i] = i*0x10101 ;
		}*/
        for ( int i = 0  ; i < 128 ; i++ ) {
            colors[2*i] = 255*0x10101 ;
        }
        long ms = System.currentTimeMillis() ;
        for ( int i = 0 ; i < 10 ; i++ ) {
            SimpleFractalPainter.paint ( img, x0, y0, dx, dy, colors, iter ) ;
        }
        ms = System.currentTimeMillis()-ms ;
        System.out.println( "paint5 time "+(ms*0.001) ) ;

    }

}
