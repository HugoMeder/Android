package de.bitsnarts.gnsstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static Globals globals ;//= new Globals () ;

    Globals getGlobals () {
        if ( globals == null )
            globals = new Globals ( this ) ;
        return globals ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}