package de.bistnarts.apps.orientationtest.tools;

public class VectorOps {
    public static double[] cross ( double[] a, double[] b ) {
        double[] rv = new double[3] ;
        rv[0] = a[1]*b[2]-a[2]*b[1] ;
        rv[1] = a[2]*b[0]-a[0]*b[2] ;
        rv[2] = a[0]*b[1]-a[1]*b[0] ;
        return rv ;
    }
    public static void normalize ( double[] v ) {
        double l = len ( v ) ;
        v[0] /= l ;
        v[1] /= l ;
        v[2] /= l ;
    }

    public static double len(double[] v) {
        return Math.sqrt( dot ( v, v ) ) ;
    }

    public static double dot(double[] a, double[] b) {
        return a[0]*b[0]+a[1]*b[1]+a[2]*b[2] ;
    }

    public static double[][] getOrthoBaseFromVector ( double v[] ) {
        v = v.clone() ;
        VectorOps.normalize( v );
        int maxInd = 0 ;
        double maxAbsVal = Math.abs( v[0] ) ;
        for ( int i = 1 ; i < 3 ; i++ ) {
            double abs = Math.abs(v[i]);
            if ( abs > maxAbsVal ) {
                maxAbsVal = abs ;
                maxInd = i ;
            }
        }
        int nextInd = maxInd + 1;
        if ( nextInd == 3 ) nextInd = 0 ;
        double[] tmp = new double[3];
        tmp[nextInd] = 1 ;
        double[][] rv = new double[3][];
        rv[0] = v ;
        rv[1] = cross( v, tmp ) ;
        rv[2] = cross( rv[0], rv[1] ) ;
        return rv ;
    }

    public static void clear(double[] v ) {
        v[0] = 0 ;
        v[1] = 0 ;
        v[2] = 0 ;
    }

    public static double[] times(double[][] matrix, double[] v) {
        double[] rv = new double[3];
        for ( int i = 0 ; i < 3 ; i++ ) {
            double sum = 0.0 ;
            for ( int j = 0 ; j < 3 ; j++ ) {
                sum += matrix[i][j]*v[j] ;
            }
            rv[i] = sum ;
        }
        return rv ;
    }

    public static double[] angleBetweenVectors(double[] a, double[] b) {
        double aLen = len(a);
        double bLen = len(b);
        double[] acb = cross(a, b);
        double cos = dot(a, b) / (aLen * bLen);
        double sin = len(acb)  / (aLen * bLen);
        double phi = Math.atan2(sin, cos);
        if ( phi == 0 ) {
            clear ( acb ) ;
        } else {
            double f = phi / sin;
            acb[0] *= f ;
            acb[1] *= f ;
            acb[2] *= f ;
        }
        return acb ;
    }

    public static double[] add(double[] a, double[] b) {
        double[] rv = new double[3];
        for ( int i = 0 ; i < 3 ; i++ ) {
            rv[i] = a[i]+b[i] ;
        }
        return rv ;
    }
}
