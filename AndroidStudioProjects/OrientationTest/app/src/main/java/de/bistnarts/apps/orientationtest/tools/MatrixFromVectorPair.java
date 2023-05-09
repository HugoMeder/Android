package de.bistnarts.apps.orientationtest.tools;

public class MatrixFromVectorPair {

    private double unit[][] = {{1.0, 0.0, 0.0},
            {0.0, 1.0, 0.0},
            {0.0, 0.0, 1.0}} ;
    private Quaternion q;
    private double phi;

    public double[][] getMatrixFromVectorPair ( double[] n1, double[] n2 ) {
        normalize( n1 );
        normalize( n2 );
        double[] ax = cross ( n1, n2 ) ;
        double a = len(ax);
        double b = dot(n1, n2);
        double phi = Math.atan2(a, b);
        this.phi = phi ;
        if ( phi == 0 ) {
            return unit ;
        }
        phi /= 2 ;
        double sin = Math.sin(phi);
        double cos = Math.cos(phi);
        normalize( ax );
        q = new Quaternion(cos, sin * ax[0], sin * ax[1], sin * ax[2]);
        double[][] rv = new double[3][3];
        q.getMatrix33( rv );
        return rv ;
    }

    private static double[] cross(double[] a, double[] b) {
        double[] rv = new double[3];
        rv[0] = a[1]*b[2]-a[2]*b[1] ;
        rv[1] = a[2]*b[0]-a[0]*b[2] ;
        rv[2] = a[0]*b[1]-a[1]*b[0] ;
        return rv ;
    }

    private static void normalize ( double[] v) {
        double l = len(v);
        v[0] /= l ;
        v[1] /= l ;
        v[2] /= l ;
    }
    private static double len(double[] n) {
        return Math.sqrt( dot ( n, n ) ) ;
    }

    private static double dot(double[] a, double[] b ) {
        return a[0]*b[0]+a[1]*b[1]+a[2]*b[2] ;
    }
}
