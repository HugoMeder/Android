package inducesmile.com.camera2;

import android.graphics.SurfaceTexture;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;

public interface CameraTasks {
    void setActivity( AppCompatActivity activity );
    void startPreview () ;
    void setPreviewTexture( TextureView texturView);
    void openCamera();
}
