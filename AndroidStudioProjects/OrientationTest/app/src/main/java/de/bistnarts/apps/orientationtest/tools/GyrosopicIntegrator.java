package de.bistnarts.apps.orientationtest.tools;

public class GyrosopicIntegrator {
    private Quaternion state;
    private long lastTime;
    private long startTime;
    private float[] lastOmega;

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
        lastTime = now ;
        lastOmega = angularVelocity ;
    }

    public void reset ( Quaternion q, long timestamp ) {
        this.state = q ;
        lastTime = timestamp ;
        startTime = System.nanoTime() ;
    }

    public Quaternion getState () {
        return state ;
    }

    public double getSecsAfterStart ()  {
        return (System.nanoTime()-startTime)/1000000000.0 ;
    }
}
