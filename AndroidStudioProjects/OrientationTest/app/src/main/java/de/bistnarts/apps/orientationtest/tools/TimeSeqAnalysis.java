package de.bistnarts.apps.orientationtest.tools;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class TimeSeqAnalysis implements SensorEventListener {

    Vector<SensorEvent> events = new Vector<SensorEvent>() ;
    TreeMap<Integer,Integer> statistics= new TreeMap<Integer,Integer>() ;

    long lastTime ;
    TimeSeqListener listener ;

    public TimeSeqAnalysis () {

    }
    public void setTimeSeqListener ( TimeSeqListener listener ) {
        this.listener = listener ;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if ( events.isEmpty() ) {
            events.add( event ) ;
            lastTime = event.timestamp ;
        } else {
            if ( lastTime != event.timestamp ) {
                if ( listener != null ) {
                    listener.timestampChanged((Vector<SensorEvent>) events.clone(), lastTime, event.timestamp );
                    int n = events.size();
                    /*if ( n > 1 ) {
                        System.out.println( "numEvents="+n);
                    }*/
                    Integer old = statistics.get(n);
                    if ( old != null ) {
                        statistics.put( n, old+1 ) ;
                    } else {
                        statistics.put( n, 1 ) ;
                    }
                }
                events.clear();
                lastTime = event.timestamp ;
            }
            events.add( event ) ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public Map<Integer,Integer> getStatistics () {
        return (Map<Integer, Integer>) statistics.clone ();
    }
}
