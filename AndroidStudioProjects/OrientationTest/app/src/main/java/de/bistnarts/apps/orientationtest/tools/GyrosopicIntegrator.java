package de.bistnarts.apps.orientationtest.tools;

import java.util.Vector;

public class GyrosopicIntegrator {

    public class AxisMeasurementResult {
        private final double[] omegaSum;
        private final double[] axis;
        private double sigma;

        AxisMeasurementResult () {
            this.omegaSum = GyrosopicIntegrator.this.omegaSum ;
            VectorOps.normalize ( omegaSum ) ;
            double[][] base = VectorOps.getOrthoBaseFromVector(omegaSum);
            int n = states.size();
            double[][] matrix = new double[3][3];
            double[][] v1 = new double[n + 1][3];
            double[][] v2 = new double[n + 1][3];
            for ( int i = 0 ; i < n ; i++ ) {
                Quaternion q = states.get(i);
                q.getMatrix33( matrix );
                v1[i] = VectorOps.times ( matrix, base[1] ) ;
                v2[i] = VectorOps.times ( matrix, base[2] ) ;
            }
            v1[n] = v1[0] ;
            v2[n] = v2[0] ;
            double[] a1 = getAreaVector ( v1 ) ;
            double[] a2 = getAreaVector ( v2 ) ;
            double area1 = VectorOps.len(a1);
            double area2 = VectorOps.len(a2);
            double pi = Math.PI;
            if ( false ) {
                VectorOps.normalize( a1 );
                VectorOps.normalize( a2 );
                double dPhi1 = VectorOps.len(VectorOps.angleBetweenVectors(omegaSum, a1)) * 180.0 / Math.PI;
                double dPhi2 = VectorOps.len(VectorOps.angleBetweenVectors(omegaSum, a2)) * 180.0 / Math.PI;
                double dPhi12 = VectorOps.len(VectorOps.angleBetweenVectors(a1, a2)) * 180.0 / Math.PI;
            }
            this.axis = VectorOps.add ( a1, a2 ) ;
            VectorOps.normalize( axis );
            sigma = 0.0;
            for ( int i = 0 ; i < n ; i++ ) {
                double d1 = VectorOps.dot(axis, v1[i]);
                double d2 = VectorOps.dot(axis, v2[i]);
                sigma += d1*d1+d2*d2 ;
            }
            sigma = Math.sqrt( sigma ) ;
        }

        public double[] getAxis () {
            return axis ;
        }

        public double getError () {
            return sigma ;
        }

        public GyrosopicIntegrator getIntegrator () {
            return GyrosopicIntegrator.this ;
        }
        private double[] getAreaVector(double[][] v) {
            int n = v.length - 1;
            double[] rv = new double[3];
            for (int i = 0; i < n; i++) {
                double[] delta = VectorOps.cross(v[i], v[i + 1]);
                for (int j = 0; j < 3; j++) {
                    rv[j] += delta[j];
                }
            }
            for (int j = 0; j < 3; j++) {
                rv[j] /= 2.0 ;
            }
            return rv;
        }
    }
    private Quaternion state;
    private long lastTime;
    private long startTime;
    private float[] lastOmega;
    private GyrosopicAxisListener listener;
    private Vector<Quaternion> states = new Vector<Quaternion> () ;
    double[] omegaSum = new double[3] ;

    public GyrosopicIntegrator (Quaternion q, long timestamp) {
        reset ( q, timestamp ) ;
    }

    public void nextSpin ( float[] angularVelocity, long timestamp ) {
        //long now = System.nanoTime();
        if ( lastTime == 0 ) {
            lastTime = timestamp ;
            lastOmega = angularVelocity ;
            return ;
        }
        long now = timestamp;
        double dt = (now - lastTime) / 1000000000.0;
        double angVel = Math.sqrt( angularVelocity[0]*angularVelocity[0]+angularVelocity[1]*angularVelocity[1]+angularVelocity[2]*angularVelocity[2]) ;
        double deltaPhi = angVel * dt;
        if ( angVel == 0 ) {
            lastTime = now ;
            lastOmega = angularVelocity ;
            return;
        }
        double s = Math.sin(deltaPhi / 2.0);
        double c = Math.cos(deltaPhi / 2.0);
        Quaternion dq = new Quaternion( c, s*angularVelocity[0]/angVel, s*angularVelocity[1]/angVel, s*angularVelocity[2]/angVel );
        state = state.times( dq ).normalize() ;
        if ( listener != null ) {
            states.add(state);
            for (int i = 0; i < 3; i++) {
                omegaSum[i] += angularVelocity[i] * dt;
            }
            double omegaSumLen = VectorOps.len( omegaSum );
            if (omegaSumLen > Math.PI * 2) {
                evaluate();
            }
        }
        lastTime = now ;
        lastOmega = angularVelocity ;
    }

    public void reset ( Quaternion q, long timestamp, GyrosopicAxisListener listener ) {
        this.state = q ;
        lastTime = timestamp ;
        startTime = System.nanoTime() ;
        this.listener = listener ;
        VectorOps.clear ( this.omegaSum ) ;
        states.clear();
    }
    public void setListener ( GyrosopicAxisListener listener ) {
        this.listener = listener ;
        state = new Quaternion( 1, 0, 0, 0 ) ;
        VectorOps.clear ( this.omegaSum ) ;
        states.clear();
        lastTime = 0 ;
    }


    public void reset ( Quaternion q, long timestamp ) {
        reset ( q, timestamp, null ) ;
    }

    public Quaternion getState () {
        return state ;
    }

    public double getSecsAfterStart ()  {
        return (System.nanoTime()-startTime)/1000000000.0 ;
    }

    private void evaluate() {
        AxisMeasurementResult result = new AxisMeasurementResult();
        GyrosopicAxisListener l = listener;
        listener = null ;
        states.clear();
        l.axisReady( result );
    }


}
