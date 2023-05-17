package de.bistnarts.apps.orientationtest.tools;

import android.app.Activity;
import android.content.Context;
import android.media.AsyncPlayer;

public class Globals {
    private Context context;
    private Activity activity ;
    private AsyncPlayer aPlayer;
    private PropertyAccess pa;

    public Globals ( Activity activity ) {
        this.context = activity.getBaseContext() ;
        this.activity = activity ;
    }
    public Context getContext () {
        return context ;
    }

    public Activity getActivity() {
        return activity ;
    }

    public AsyncPlayer getAsyncPlayer() {
        if ( aPlayer == null )
            aPlayer = new AsyncPlayer( "player" ) ;
        return aPlayer ;
    }

    public PropertyAccess getPropertyAccess() {

        if ( pa == null ) {
            pa = new PropertyAccess( context ) ;
        }
        return pa ;
    }
}
