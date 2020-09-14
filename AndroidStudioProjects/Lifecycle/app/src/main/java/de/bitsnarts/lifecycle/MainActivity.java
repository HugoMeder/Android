package de.bitsnarts.lifecycle;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import de.bitsnarts.android.utils.communication.BNAPrintlnService;

public class MainActivity extends AppCompatActivity {

    static int nextInstanceNr ;
    private final int instanceNr;
    private TextView textView;
    private static BNAPrintlnService println = new BNAPrintlnService() ;

    MainActivity () {
        super () ;
        this.instanceNr = nextInstanceNr++ ;
    }

    private void log(String s) {
        println.println( s );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textView.setText ( "onCreate, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
        log(  "onCreate, instance "+instanceNr+"\nprintln-state="+println.getState() );
    }

    @Override
    protected void onStart() {
        super.onStart();
        log ( "onStart, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @Override
    protected void onResume() {
        super.onResume();
        log ( "onResume, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @Override
    protected void onPause() {
        super.onPause();
        log ( "onPause, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @Override
    protected void onStop() {
        super.onStop();
        log ( "onStop, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log ( "onDestroy, instance "+instanceNr+"\nprintln-state="+println.getState() ) ;
    }
}
