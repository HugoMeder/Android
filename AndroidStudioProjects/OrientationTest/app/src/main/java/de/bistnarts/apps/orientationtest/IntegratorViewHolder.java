package de.bistnarts.apps.orientationtest;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.net.Uri;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import de.bistnarts.apps.orientationtest.tools.Globals;

public class IntegratorViewHolder extends AbstractViewHolder {
    private final IntegratorDisplay integratorDisplay;

    public IntegratorViewHolder(View itemView, Globals globals ) {
        super(itemView, globals ) ;
        ConstraintLayout view = (ConstraintLayout) itemView;
        integratorDisplay = (IntegratorDisplay) view.getViewById(R.id.integratorView);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch ( type ) {
            case Sensor.TYPE_ACCELEROMETER:
                integratorDisplay.setAccelleration ( event.values, event.timestamp ) ;
                break ;
            case Sensor.TYPE_GYROSCOPE:
                integratorDisplay.setAngularVelocity ( event.values, event.timestamp ) ;
                break ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void attach() {
        super.attach();
        if ( integratorDisplay != null )
            integratorDisplay.attach();
    }
}
