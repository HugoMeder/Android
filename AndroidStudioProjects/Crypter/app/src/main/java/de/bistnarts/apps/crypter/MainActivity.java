package de.bistnarts.apps.crypter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Vector;

import de.bitsnarts.chiffer.Decoder;

public class MainActivity extends AppCompatActivity {

    private ClipboardManager clipboardManager;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123, savedInstanceState );
        clipboardManager = (ClipboardManager) getSystemService( CLIPBOARD_SERVICE );

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