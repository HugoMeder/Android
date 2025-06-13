package inducesmile.com.camera2;

import android.graphics.SurfaceTexture;
import android.os.Handler;
//import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;

public interface CameraTasks {
    void setActivity( androidx.appcompat.app.AppCompatActivity activity );
    void startPreview () ;
    void setPreviewTexture( TextureView texturView);
    void openCamera();
    void closeCamera();
    void log(String s);
    Handler getHandler () ;
    void takePicture();

}
