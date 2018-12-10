package de.bitsnarts.android.apps.fractals.graphics;

public class Mandelbrot implements FractalIterator {

	
	private int maxIteration;
	private double critAbsValSqr;

	public Mandelbrot ( int maxIteration, double critAbsVal ) {
		this.maxIteration = maxIteration ;
		this.critAbsValSqr = critAbsVal*critAbsVal ;
	}
	
	@Override
	public int iteratationCount(double x, double y) {
		double cre = x ;
		double cim = y ;
		int rv = 0 ;
		while ( rv < maxIteration ) {
			if ( x*x + y*y >= critAbsValSqr )
				return rv ;
			double x_ = x*x-y*y+cre ;
			y = 2*x*y +cim ;
			x = x_ ;
			rv++ ;
		}
		return rv ;
	}

}
