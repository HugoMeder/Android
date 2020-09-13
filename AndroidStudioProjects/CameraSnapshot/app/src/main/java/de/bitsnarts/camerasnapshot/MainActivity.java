package de.bitsnarts.camerasnapshot;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.TextureView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import de.bitsnarts.BNASockets.BNAServerSocket;
import de.bitsnarts.CameraService.Model.ServiceModel;
import de.bitsnarts.CameraService.ServiceBinder;
import de.bitsnarts.CameraService.ServiceImpl;
import de.bitsnarts.CameraService.ServiceListener;

public class MainActivity extends AppCompatActivity implements ServiceListener, TextureView.SurfaceTextureListener {

    private boolean cameraEventReceived;
    private CameraDevice camera;
    private TextureView textureView;
    private SurfaceTexture surfaceTexture;

    MainActivity () {
        super () ;
        service = new ServiceImpl () ;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        textView.setText( "onSurfaceTextureAvailable");
        surfaceReady ( textureView.getSurfaceTexture() ) ;
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

    class CameraCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            //textView.setText("onOpened");
            cameraConnected ( cameraDevice ) ;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            //textView.setText("onDisconnected");
            cameraDisconnected ( cameraDevice ) ;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            //textView.setText("onError");
            cameraError ( i ) ;
        }

    }

    class CameraHandler extends Handler {
 /*
        public void dispatchMessage(Message msg) {
            super.dispatchMessage( msg );
            cameraEvent ( msg ) ;
            textView.setText("message") ;
        }
*/
    }

    Connection connection;
    TextView textView;
    ServiceModel service;
    UpdateState us = new UpdateState();
    CameraCallback cameraCallback = new CameraCallback () ;
    CameraHandler cameraHandler = new CameraHandler () ;

    class UpdateState implements Runnable {
        public void run() {
            updateState();
        }
    }

    class Connection implements ServiceConnection {

        private ServiceModel service;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //textView.setText( "service connected!");
            ServiceBinder b = (ServiceBinder) service;
            this.service = b.getService();
            //textView.setText( "service connected! serv "+this.service );
            addListener();
            //this.service.requestCallbacks();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        public ServiceModel getService() {
            return service;
        }
    }

    ;


    void addListener() {
        service = connection.service;
        service.addServiceListener(this);
        //textView.setText("listener added serv " + service + ", state " + service.getState());
        //serviceThread.halt () ;
        if ( camera != null ) {
            service.setCamera( camera );
        }
        service.start();
    }

    @TargetApi(Build.VERSION_CODES.P)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = (TextView) findViewById(R.id.textView);
        textView.setBackgroundColor( 0xffffff00 );

        textureView = (TextureView)findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener( this );

        textView.setText("onCreate");

        requestPermissions(new String[]{
                        Manifest.permission.INTERNET}
                , 10);

        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_GRANTED) {
            textView.setText("intent");

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    //        .setAction("Action", null).show();
                    //textView.setText("clicked");
                    startPreview () ;
                }

            });

            try {
                BNAServerSocket ss = new BNAServerSocket(10);
                /*

                InetAddress addr = BNALookup.getAddress() ;
                textView.setText( "addr "+addr );
                */
                if (false) {
                    Socket s = ss.accept();
                }

            } catch (IOException e) {
                textView.setText(e.toString());
                e.printStackTrace();
            }
        } else {
            textView.setText("internet denied");
        }

        /*connection = new Connection () ;
        Intent intent = new Intent(this,
                ServiceImpl.class);

        boolean rv = bindService(intent, connection, Context.BIND_AUTO_CREATE);
        */

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        textView.setText("cameraManager");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getCamera();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void getCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String id = null;
        try {
            id = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
            textView.setText( e.toString() );
            return ;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                if ( true )
                    manager.openCamera(id, cameraCallback, cameraHandler );
            } catch (CameraAccessException e) {
                e.printStackTrace();
                textView.setText( e.toString() );
            }
            if ( camera != null )
                textView.setText( "wir haben eine kamera!" );
            else
                textView.setText( "wir haben KEINE kamera!" );
        } else {
            textView.setText( "camera not granted" );
        }
    }

    private void cameraDisconnected(CameraDevice cameraDevice) {
    }

    private void startPreview() {
        try {
            if ( service != null ) {
                service.startPreview () ;
            } else {
                textView.setText( "service == null" );
            }
        } catch (Exception e) {
            textView.setText( e.toString() );
        }
    }

    private void cameraConnected(CameraDevice cameraDevice) {
        synchronized ( this ) {
            this.camera = cameraDevice ;
            textView.setText( "cameraConnected "+camera );
        }
        if ( service != null ) {
            service.setCamera ( cameraDevice ) ;
        } else {
            textView.setText( "cameraConnected, service == null" );
        }
    }

    private void cameraError( int error ) {
    }

    private void cameraEvent(Message msg) {
        synchronized ( this ) {
            cameraEventReceived = true ;
            this.notifyAll();
        }
    }


    protected void onStart() {
        super.onStart();
        if ( true ) {
            textView.setText( "onStart" );
            if ( service != null ) {
                service.start();
            } else {
                textView.setText( "service == null" );
            }
        }
    }

    protected void onStop() {
        super.onStop();
        if ( service != null ) {
            textView.setText( "onStop" );
            service.halt () ;
        }
    }


    // Function to check and request permission
    public void checkPermission(String permission, int requestCode)
    {

        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                MainActivity.this,
                permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat
                    .requestPermissions(
                            MainActivity.this,
                            new String[] { permission },
                            requestCode);
        }
        else {
            Toast
                    .makeText(MainActivity.this,
                            "Permission already granted",
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }

    String exeptionToStr ( Throwable th ) {
        CharArrayWriter cs = new CharArrayWriter () ;
        try (PrintWriter pw = new PrintWriter(cs)) {
            th.printStackTrace( pw );
            pw.flush();
            return cs.toString () ;
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void surfaceReady(SurfaceTexture surfaceTexture) {
        textView.setText( "setSurfaceTexture...") ;
        synchronized ( this ) {
            this.surfaceTexture = surfaceTexture ;
        }
        if ( service != null ) {
            service.setSurfaceTexture ( surfaceTexture ) ;
        } else {
            textView.setText( "setSurfaceTexture not possible, service == null") ;
        }
    }
    void updateState () {
        textView.setText ( "state "+service.getState () ) ;
    }

    @Override
    public void stateChanged ( ServiceModel service ) {
        runOnUiThread ( us ) ;
    }
}
