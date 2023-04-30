package de.bistnarts.apps.orientationtest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

public class SpiritLevelViewHolder extends AbstractViewHolder {
    private final SpiritLevelDisplay spiritlevelDisplay;

    public SpiritLevelViewHolder(View itemView) {
        super(itemView);
        ConstraintLayout view = (ConstraintLayout) itemView;
        spiritlevelDisplay = (SpiritLevelDisplay) view.getViewById(R.id.spiritlevelView);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch ( type ) {
            case Sensor.TYPE_ROTATION_VECTOR:
                spiritlevelDisplay.setOrientation ( event.values ) ;
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
