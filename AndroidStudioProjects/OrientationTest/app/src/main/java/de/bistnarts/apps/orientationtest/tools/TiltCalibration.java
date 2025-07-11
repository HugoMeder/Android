package de.bistnarts.apps.orientationtest.tools;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;
import java.util.Vector;

public class TiltCalibration {

    private final Context context;
    private Vector<Event> acc = new Vector<Event>() ;
    private Vector<Event> omega = new Vector<Event>() ;
    public double[] axis;
    public double angle;
    public double angleError;

    private class Event implements Comparable<Event> {

        private final float[] values;
        private final long time;

        Event(float[] values, long timestamp ) {
            this.values = values ;
            this.time = timestamp ;
        }

        @Override
        public int compareTo(Event o) {
            if ( o.time == time )
                return 0 ;
            if ( o.time > time )
                return 1 ;
            else
                return -1 ;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if ( obj == null )
                return false ;
            if ( obj instanceof Event)
                return compareTo((Event) obj) == 0 ;
            else
                return false ;
        }
    }

    public TiltCalibration(Context contxt) {
        this.context = contxt ;
    }
    public void setAcceleration ( float[] a, long timestampnano ) {
        acc.add( new Event( a, timestampnano) ) ;
    }

    public void setAngularVelocity ( float[] omega, long timestampnano ) {
        this.omega.add( new Event( omega, timestampnano) ) ;
    }

    private void storeData() {
        File dir = context.getExternalFilesDir(null);
        File file = new File(dir, "tiltData.bin");
        try {
            FileOutputStream out = new FileOutputStream(file);
            DataOutputStream dout = new DataOutputStream(out);
            dout.writeInt ( 1 ) ;
            writeEvents ( dout, this.acc ) ;
            writeEvents ( dout, this.omega ) ;
            dout.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEvents(DataOutputStream dout, Vector<Event> events ) throws IOException {
        dout.writeInt( events.size() );
        for ( Event e : events ) {
            dout.writeLong( e.time );
            dout.writeInt( e.values.length );
            for ( float f : e.values ) {
                dout.writeFloat( f );
            }
        }
    }
    public void evaluate () {
        storeData () ;
        evaluateOmega () ;
        evaluateAcc () ;
    }

    private void evaluateAcc() {
        double[] acc = new double[3] ;
        double phiSum = 0 ;
        double phiSumSqr = 0 ;
        for ( Event e : this.acc) {
            acc[0] = e.values[0] ;
            acc[1] = e.values[1] ;
            acc[2] = e.values[2] ;
            phiSum += VectorOps.len( VectorOps.angleBetweenVectors( axis, acc ) ) ;
        }
        int n = this.acc.size();
        phiSum /= n ;
        for ( Event e : this.acc) {
            acc[0] = e.values[0] ;
            acc[1] = e.values[1] ;
            acc[2] = e.values[2] ;
            double phi = VectorOps.len( VectorOps.angleBetweenVectors(axis, acc ) );
            double delta = phi - phiSum;
            phiSumSqr += delta*delta ;
        }
        this.angle = phiSum ;
        this.angleError = Math.sqrt ( phiSumSqr/n ) ;
    }

    private void evaluateOmega() {
        double[] omegaSum = null ;
        double[] omegaStart = null ;
        long lastTime = 0 ;
        for ( Event e : omega ) {
            if ( omegaSum == null ) {
                omegaSum = new double[3] ;
                omegaStart = new double[3];
                omegaStart[0] += e.values[0] ;
                omegaStart[1] += e.values[1] ;
                omegaStart[2] += e.values[2] ;
                lastTime = e.time ;
            } else {
                double dt = (e.time - lastTime) / 10000000000.0;
                dt = VectorOps.dot( omegaStart, e.values ) > 0 ? dt : -dt ;
                omegaSum[0] += e.values[0]*dt ;
                omegaSum[1] += e.values[1]*dt ;
                omegaSum[2] += e.values[2]*dt ;
                lastTime = e.time ;
            }
        }
        VectorOps.normalize( omegaSum );
        if ( omegaSum[1] < 0  ) {
            omegaSum[0] = -omegaSum[0] ;
            omegaSum[1] = -omegaSum[1] ;
            omegaSum[2] = -omegaSum[2] ;
        }
        this.axis = omegaSum ;
    }
}
