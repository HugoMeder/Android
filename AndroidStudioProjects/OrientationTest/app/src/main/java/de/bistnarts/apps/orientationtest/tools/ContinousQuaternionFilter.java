package de.bistnarts.apps.orientationtest.tools;

public class ContinousQuaternionFilter {
    private float[] last;

    public void map (float[] q ) {
        if ( last == null ) {
            copyToLast ( q ) ;
            return ;
        }
        double dot = 0.0;
        for ( int i = 0 ; i < 4 ; i++ ) {
            dot += last[i]*q[i] ;
        }
        if ( dot < 0 ) {
            for ( int i = 0 ; i < 4 ; i++ ) {
                q[i] = -q[i] ;
            }
        }
        copyToLast( q );
    }

    private void copyToLast(float[] q) {
        if ( last == null )
            last = new float[4] ;
        for ( int i = 0 ; i < 4 ; i++ ) {
            last[i] = q[i] ;
        }
    }
}
