package de.bitsnarts.android.apps.fractals;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.UUID;

import de.bitsnarts.android.tools.logger.Logger;

public class MainActivity extends AppCompatActivity {

    private MainView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.log().println ( "Started !" ) ;
        super.onCreate(savedInstanceState);
        setContentView( de.bitsnarts.android.apps.fractals.R.layout.activity_main);
        view = (MainView)findViewById(R.id.drawing);
    }

    @Override
    public void onDestroy () {
        Logger.finish() ;
        super.onDestroy();
        //super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        int gid = item.getGroupId() ;
        System.out.println ( "O gid "+gid ) ;
        if ( gid == R.id.coloring ) {
            coloringAction ( item ) ;
            return true ;
        }
        switch ( gid ) {
        case R.id.coloring:
            coloringAction ( item ) ;
            return true ;
        case R.id.image:
            imageAction ( item ) ;
            return true ;
        }
        return false ;
    }

    private void coloringAction(MenuItem item) {
        int id = item.getItemId();
        view.setColoring ( id ) ;
    }

    private void imageAction(MenuItem item) {
        int id = item.getItemId();
        if ( id == R.id.file_save )
            storeImage () ;
    }

    private void storeImage() {
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle( R.string.saveImage );
            saveDialog.setMessage( R.string.saveImageToGaleryImage);
            saveDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    //drawView.setDrawingCacheEnabled(true);
                    Bitmap bm = view.getBitmap() ;
                    if ( bm == null ) {
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Oops! bm is null.", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    String filename = UUID.randomUUID().toString()+".png" ;
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), bm,
                            filename, "drawing");
                    if(imgSaved!=null){
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                R.string.imageSaved+" "+filename+"!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                R.string.imageNotSaved, Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                }
            });
            saveDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();
    }
}
