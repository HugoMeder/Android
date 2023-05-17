package de.bistnarts.apps.orientationtest;

import android.app.Activity;
import android.hardware.SensorEventListener;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import de.bistnarts.apps.orientationtest.tools.Globals;

abstract class AbstractViewHolder extends RecyclerView.ViewHolder implements SensorEventListener, AttachDetach {

    //protected final Activity activity;
    protected final Globals globals;
    private boolean attached;

    public AbstractViewHolder(View itemView, Globals globals ) {
        super(itemView);
        this.globals = globals ;
        //this.activity =  activity ;
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