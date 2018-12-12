package de.bitsnarts.android.apps.fractals;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( de.bitsnarts.android.apps.fractals.R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onContextItemSelected (MenuItem item) {
        int gid = item.getGroupId() ;
        int id = item.getItemId();
        System.out.println ( "gid "+gid+", id "+id ) ;
        if ( gid == R.id.coloring ) {
            if ( id == R.id.coloring_bw) {
                System.out.println ( "BW" ) ;
            } else if ( id == R.id.coloring_kernel ) {
                System.out.println ( "KERNEL" ) ;
            }
        }
        if ( id == R.id.coloring_bw) {
            System.out.println ( "BW" ) ;
        } else if ( id == R.id.coloring_kernel ) {
            System.out.println ( "KERNEL" ) ;
        }
        return true ;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        int gid = item.getGroupId() ;
        System.out.println ( "O gid "+gid ) ;
        if ( gid == R.id.coloring ) {
            coloringAction ( item ) ;
            return true ;
        }
        return false ;
    }

    private void coloringAction(MenuItem item) {
        int id = item.getItemId();
        if ( id == R.id.coloring_bw) {
            System.out.println ( "BW" ) ;
        } else if ( id == R.id.coloring_kernel ) {
            System.out.println ( "KERNEL" ) ;
        }
    }
}
