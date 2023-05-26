package de.bistnarts.apps.orientationtest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.AttributeSet;
import android.view.View;

import de.bistnarts.apps.orientationtest.tools.Globals;

public class TaschenlampenView extends View implements AttachDetach, View.OnLongClickListener {
    private boolean attached;
    private Globals globals;
    private Camera cam;
    private boolean lichtan;
    private CameraManager mCameraManager;
    private String mCameraId;

    public TaschenlampenView(Context context) {
        super(context);
        init () ;
    }

    public TaschenlampenView(Context context, AttributeSet attrs ) {
        super(context, attrs );
        init () ;
    }

    private void init() {
        setOnLongClickListener( this );
    }

    @Override
    public void attach() {
        attached = true ;
    }

    @Override
    public void detach() {
        attached = false ;
    }

    @Override
    public boolean isAttached() {
        return attached;
    }

    public void setGlobals(Globals globals) {
        this.globals = globals ;
        mCameraManager = (CameraManager) globals.getActivity().getSystemService(Context.CAMERA_SERVICE);
        boolean isFlashAvailable = globals.getActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        if ( !isFlashAvailable )
            return ;
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
            //mCameraManager.openCamera( mCameraId, this );
        } catch (CameraAccessException e) {
            return;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (globals.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {

            if (cam == null) {

                //cam = Camera.open();
            }
            if (lichtan) {
                //am.stopPreview();
                try {
                    mCameraManager.setTorchMode ( mCameraId, false ) ;
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
                lichtan = false ;
            } else {
                /*
                Camera.Parameters param = cam.getParameters();
                param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(param);
                cam.startPreview();
                */
                try {
                    mCameraManager.setTorchMode ( mCameraId, true ) ;
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
                lichtan = true;
            }
        }
        return false;
    }
}
