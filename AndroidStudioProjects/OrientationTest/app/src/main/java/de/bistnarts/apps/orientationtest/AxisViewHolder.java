package de.bistnarts.apps.orientationtest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

public class AxisViewHolder extends AbstractViewHolder {
    private final AxisDisplay axisDisplay;

    public AxisViewHolder(View itemView, Activity activity) {
        super(itemView, activity);
        ConstraintLayout view = (ConstraintLayout) itemView;
        axisDisplay = (AxisDisplay) view.getViewById(R.id.axisView);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch ( type ) {
            case Sensor.TYPE_ROTATION_VECTOR:
                axisDisplay.setOrientation ( event.values ) ;
                break ;
            case Sensor.TYPE_MAGNETIC_FIELD:
                axisDisplay.setMagneticField ( event.values ) ;
                break ;
            case Sensor.TYPE_ACCELEROMETER:
                axisDisplay.setAccelleration ( event.values ) ;
                break ;
            case Sensor.TYPE_GYROSCOPE:
                axisDisplay.setAngularVelocity ( event.values, event.timestamp ) ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void attach() {
        super.attach();
        axisDisplay.attach();
    }
}
