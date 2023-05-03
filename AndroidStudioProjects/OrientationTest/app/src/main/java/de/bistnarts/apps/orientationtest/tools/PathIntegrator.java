package de.bistnarts.apps.orientationtest.tools;

public class PathIntegrator {

    private final GyrosopicIntegrator gi;
    private long lastAcc;
    private double[][] matrix = new double[3][3] ;
    private double[] accWS = new double[3] ;
    private double[] velWS = new double[3] ;
    private double[] posWS = new double[3] ;

    public PathIntegrator (Quaternion q, double[] position, long nanoseconds ) {
        gi = new GyrosopicIntegrator( q, nanoseconds ) ;
        q.getMatrix33( matrix );
        lastAcc = nanoseconds ;
        //lastOmega = nanoseconds ;
    }

    public void setAcceleration ( float[] acceleration, long nanoseconds ) {
        if ( lastAcc == 0.0 ) {
            lastAcc = nanoseconds ;
            return ;
        }
        mkAccWS ( acceleration ) ;
        double dt = (nanoseconds - lastAcc) / 1000000000.0;
        for ( int i = 0 ; i < 3 ; i++ ) {
            double a = accWS[i];
            double v = velWS[i];
            v += a*dt ;
            double p = posWS[i];
            p += v*dt ;
            velWS[i] = v ;
            posWS[i] = p ;
        }
        lastAcc = nanoseconds ;
    }

    public void setAngularVelocity ( float[] omega, long timestamp ) {
        gi.nextSpin( omega, timestamp );
        gi.getState().getMatrix33( matrix );
    }

    private void mkAccWS( float[] accBS ) {
        for ( int i = 0 ; i < 3 ; i++ ) {
            double sum = 0.0 ;
            for ( int j = 0 ; j<3 ; j++ ) {
                sum+= matrix[i][j]*accBS[j] ;
            }
            accWS[i] = sum ;
        }
    }

    public GyrosopicIntegrator getGyrosopicIntegrator() {
        return gi;
    }

    public double[] getPositionWS () {
        return posWS ;
    }
    public double[] getVeloctiyWS () {
        return velWS ;
    }
    public  double[] getAccelerationWS () {
        return accWS ;
    }

    public void reset(Quaternion orientation, double[] position, double[] velocity, long timestamp ) {
        gi.reset( orientation, timestamp );
        if ( position != null ) {
            for ( int i = 0 ; i < 3 ; i++ ){
                posWS[i] = position[i];
            }
        } else {
            for ( int i = 0 ; i < 3 ; i++ ) {
                posWS[i] = 0;
            }
        }
        if ( velocity != null ) {
            for ( int i = 0 ; i < 3 ; i++ ){
                velWS[i] = velocity[i] ;
            }
        } else {
            for ( int i = 0 ; i < 3 ; i++ ) {
                velWS[i] = 0;
            }
        }
        this.lastAcc = timestamp ;
   }
}
