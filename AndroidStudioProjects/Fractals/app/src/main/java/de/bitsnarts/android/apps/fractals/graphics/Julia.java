package de.bitsnarts.android.apps.fractals.graphics;

import de.bitsnarts.android.apps.fractals.graphics.FractalIterator;

public class Julia implements FractalIterator {

    private final double cre;
    private final double cim;
    private final int maxIteration;
    private final double critAbsValSqr;

    public Julia ( int maxIteration, double critAbsVal, double cre, double cim ) {
        this.cre = cre ;
        this.cim = cim ;
        this.maxIteration = maxIteration ;
        this.critAbsValSqr = critAbsVal*critAbsVal ;
    }

    @Override
    public int iteratationCount(double x, double y) {

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
