package de.bistnarts.apps.orientationtest.tools;

import android.hardware.SensorEvent;

import java.util.Vector;

public interface TimeSeqListener {
    void timestampChanged  (Vector<SensorEvent> events, long lastTimestamp, long newTimestamp ) ;
}
