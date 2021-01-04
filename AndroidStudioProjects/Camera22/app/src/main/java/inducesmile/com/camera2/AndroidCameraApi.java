package inducesmile.com.camera2;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
// import android.util.Log;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

public class AndroidCameraApi extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    static CameraTasks cameraTasks = new CameraTasksImpl () ;
    private TextureView textureView;
    private String TAG = "AndroidCameraApi" ;

    class SC implements Runnable {

        @Override
        public void run() {
            cameraTasks.setPreviewTexture( textureView ) ;
            cameraTasks.openCamera () ;
        }
    }

    class TakePicture implements Runnable {

        @Override
        public void run() {
            cameraTasks.takePicture () ;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i ( TAG, "onCreate") ;
        setContentView(R.layout.activity_android_camera_api);
        textureView = (TextureView) findViewById(R.id.texture);
        textureView.setSurfaceTextureListener( this );
        cameraTasks.setActivity( this );

        Button takePictureButton = (Button) findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        cameraTasks.closeCamera () ;
        Log.i(TAG, "return from cameraTasks.closeCamera()");
        super.onDestroy();

    }


    private void takePicture() {
        cameraTasks.getHandler().post( new TakePicture() ) ;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        //runOnUiThread( new SC() );
        cameraTasks.getHandler().post( new SC() ) ;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.i ( TAG, "onSurfaceTextureSizeChanged" ) ;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.i ( TAG, "onSurfaceTextureDestroyed" ) ;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Log.i ( TAG, "onSurfaceTextureUpdated" ) ;
    }
}