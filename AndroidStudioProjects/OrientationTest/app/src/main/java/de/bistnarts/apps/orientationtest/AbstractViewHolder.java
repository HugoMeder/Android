package de.bistnarts.apps.orientationtest;

import android.app.Activity;
import android.hardware.SensorEventListener;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

abstract class AbstractViewHolder extends RecyclerView.ViewHolder implements SensorEventListener, AttachDetach {

    protected final Activity activity;
    private boolean attached;

    public AbstractViewHolder(View itemView, Activity activity ) {
        super(itemView);
        this.activity =  activity ;
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

    public boolean onContextItemSelected(MenuItem item) {
        return false;
    }
}