package de.bistnarts.apps.orientationtest.tools;

public class GyrosopicIntegrator {
    private Quaternion state;
    private long lastTime;

    public GyrosopicIntegrator (Quaternion q) {
        reset ( q ) ;
    }

    public void nextSpin ( float[] angularVelocity ) {
        long now = System.nanoTime();
        double dt = (now - lastTime) / 1000000000.0;
        double angVel = Math.sqrt( angularVelocity[0]*angularVelocity[0]+angularVelocity[1]*angularVelocity[1]+angularVelocity[2]*angularVelocity[2]) ;
        double deltaPhi = angVel * dt;
        if ( angVel == 0 ) {
            lastTime = now ;
            return;
        }
        double s = Math.sin(deltaPhi / 2.0);
        double c = Math.cos(deltaPhi / 2.0);
        Quaternion dq = new Quaternion( c, s*angularVelocity[0]/angVel, s*angularVelocity[1]/angVel, s*angularVelocity[2]/angVel );
        state = state.times( dq ).normalize() ;
        lastTime = now ;
    }

    public void reset ( Quaternion q ) {
        this.state = q ;
        lastTime = System.nanoTime() ;
    }

    public Quaternion getState () {
        return state ;
    }

}
