package de.bistnarts.apps.orientationtest;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import de.bistnarts.apps.orientationtest.tools.Globals;

public class TaschenlampenViewHolder extends AbstractViewHolder {

    private final TaschenlampenView taschenlampenView;

    public TaschenlampenViewHolder(View itemView, Globals globals ) {
        super(itemView, globals);
        ConstraintLayout view = (ConstraintLayout) itemView;
        taschenlampenView = (TaschenlampenView) view.getViewById(R.id.taschenlampenView);
        taschenlampenView.setGlobals ( globals );

    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
