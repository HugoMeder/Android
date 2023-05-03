package de.bistnarts.apps.orientationtest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

public class IntegratorViewHolder extends AbstractViewHolder {
    private final IntegratorDisplay integratorDisplay;

    public IntegratorViewHolder(View itemView) {
        super(itemView);
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
