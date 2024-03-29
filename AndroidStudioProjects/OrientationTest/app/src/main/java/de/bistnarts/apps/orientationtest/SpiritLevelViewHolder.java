package de.bistnarts.apps.orientationtest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import de.bistnarts.apps.orientationtest.tools.Globals;

public class SpiritLevelViewHolder extends AbstractViewHolder {
    private final SpiritLevelDisplay spiritlevelDisplay;

    public SpiritLevelViewHolder(View itemView, Globals globals ) {
        super(itemView, globals );
        ConstraintLayout view = (ConstraintLayout) itemView;
        spiritlevelDisplay = (SpiritLevelDisplay) view.getViewById(R.id.spiritlevelView);
        spiritlevelDisplay.setGlobals ( globals );
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return spiritlevelDisplay.onContextItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch ( type ) {
            case Sensor.TYPE_ACCELEROMETER:
                spiritlevelDisplay.setAcceleration( event.values, event.timestamp ) ;
                break ;
            case Sensor.TYPE_GYROSCOPE:
                spiritlevelDisplay.setAngularVelocity ( event.values, event.timestamp ) ;
                break ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void attach() {
        super.attach();
        spiritlevelDisplay.attach();
    }
}
