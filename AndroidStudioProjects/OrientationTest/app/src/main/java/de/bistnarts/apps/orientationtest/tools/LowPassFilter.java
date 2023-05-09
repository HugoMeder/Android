package de.bistnarts.apps.orientationtest.tools;

public class LowPassFilter {
    private final double decayTime;
    private double[] accum;
    private long lastTime;
    private long startTime;
    private float[] singleIn;

    public LowPassFilter (double decayTimeSeconds ) {
        this.decayTime = decayTimeSeconds ;
    }

    public float[] filter (float[] values ) {
        long ms = System.currentTimeMillis();
        int n = values.length;
        if (accum == null) {
            accum = new double[n];
            for (int i = 0; i < n; i++) {
                accum[i] = values[i];
            }
            lastTime = ms;
            startTime = ms;
            return values;
        } else {
            double sec = ((ms - lastTime) / 1000.0);
            //System.out.println( "sec "+sec);
            double b = sec / decayTime;
            double a = 1.0 - b;
            //System.out.println( "a "+a+", b "+b);

            float[] rv = new float[n];
            for (int i = 0; i < n; i++) {
                double val = values[i];
                double acc = accum[i];
                double newVal = a * acc + b * val;
                //System.out.println( "delta "+(newVal-val) );
                accum[i] = newVal;
                rv[i] = (float) newVal;
            }
            lastTime = ms;
            return rv;
        }
    }
    public float filter ( double value ) {
        if ( singleIn == null ) {
            singleIn = new float[1] ;
        }
        singleIn[0] = (float) value;
        float[] rv = filter(singleIn);
        return rv[0] ;
    }

    public void reset() {
        accum = null ;
    }

    public double getTimeSincveStart () {
        if ( accum == null )
            return 0.0;
        return (lastTime-startTime)/1000.0 ;
    }
}
