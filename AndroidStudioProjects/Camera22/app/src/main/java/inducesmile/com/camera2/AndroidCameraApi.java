package inducesmile.com.camera2;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
// import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

public class AndroidCameraApi extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    static CameraTasks cameraTasks = new CameraTasksImpl () ;
    private TextureView textureView;

    class SC implements Runnable {

        @Override
        public void run() {
            cameraTasks.setPreviewTexture( textureView ) ;
            cameraTasks.openCamera () ;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    private void takePicture() {
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        runOnUiThread( new SC() );
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}