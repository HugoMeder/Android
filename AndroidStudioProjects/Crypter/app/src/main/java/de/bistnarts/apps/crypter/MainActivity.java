package de.bistnarts.apps.crypter;

import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import de.bitsnarts.chiffer.Decoder;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.P)
    class Callback extends BiometricPrompt.AuthenticationCallback {

        Callback () {
            super () ;
        }
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            Toast.makeText(getApplicationContext(),
                            "Authentication error: " + errString, Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onAuthenticationSucceeded(
                BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            Toast.makeText(getApplicationContext(),
                    "Authentication succeeded!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            Toast.makeText(getApplicationContext(), "Authentication failed",
                            Toast.LENGTH_SHORT)
                    .show();
        }

    }
    private ClipboardManager clipboardManager;
    private Bundle savedInstanceState;
    private BiometricPrompt promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clipboardManager = (ClipboardManager) getSystemService( CLIPBOARD_SERVICE );
        this.savedInstanceState = savedInstanceState ;
        //auth () ;
        launchFileSelection () ;
    }
/*
    @RequiresApi(api = Build.VERSION_CODES.P)
    void auth () {
        BiometricPrompt.Builder b = new BiometricPrompt.Builder(getBaseContext());
        promptInfo = b
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();
        BiometricPrompt.CryptoObject co = MainActivity.this;
        Executor executor = ContextCompat.getMainExecutor(this);

        promptInfo.authenticate( this, executor, new Callback() );
    }

 */
    @SuppressWarnings("deprecation")
    void launchFileSelection () {
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123, savedInstanceState );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        InputStream in = getInputStreamFromUri ( uri ) ;
        try {
            String txt = getTextFrom ( in, "topkapi" ) ;
            TextView tw = (TextView) findViewById(R.id.textView);
            tw.setText( txt );
            ClipData rv = ClipData.newPlainText("label", txt);
            clipboardManager.setPrimaryClip( rv );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTextFrom(InputStream in, String password ) throws IOException {
        Decoder d = new Decoder( in, password) ;
        DataInputStream dinp = new DataInputStream(d);
        int pwl = dinp.readInt();
        if ( pwl != password.length() ) {
            dinp.close();
            return null ;
        }
        String pw = dinp.readUTF();
        if ( !pw.equals( password ) ) {
            dinp.close();
            return null ;
        }
        String rv = dinp.readUTF();
        dinp.close();
        return rv ;
    }

    private InputStream getInputStreamFromUri(Uri uri) {
        ContentResolver cr = getContentResolver();
        try{
            InputStream in = cr.openInputStream(uri);
            return in ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}