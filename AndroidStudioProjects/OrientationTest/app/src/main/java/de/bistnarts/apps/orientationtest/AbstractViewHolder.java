package de.bistnarts.apps.orientationtest;

import android.hardware.SensorEventListener;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

abstract class AbstractViewHolder extends RecyclerView.ViewHolder implements SensorEventListener, AttachDetach {

    private boolean attached;

    public AbstractViewHolder(View itemView ) {

        super(itemView);
    }

    @Override
    public void attach() {
        attached = true;
    }

    @Override
    public void detach() {
        attached = false;
    }

    @Override
    public boolean isAttached() {
        return attached ;
    }

}