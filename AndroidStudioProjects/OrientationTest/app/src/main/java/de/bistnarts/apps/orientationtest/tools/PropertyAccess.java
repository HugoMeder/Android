package de.bistnarts.apps.orientationtest.tools;

import android.content.Context;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class PropertyAccess {
    private final Properties props;
    private final File file;

    public PropertyAccess (Context context ) {

        // /data/user/0/de.bistnarts.apps.orientationtest/files
        File dir = context.getFilesDir();
        dir = context.getExternalFilesDir( null );
        // /storage/emulated/0/Android/data/de.bistnarts.apps.orientationtest/files
        //File dir = context.getDir( ".properties", Context.MODE_PRIVATE ) ;
        dir = new File ( dir, ".properties" ) ;
        if ( !dir.exists() )
            dir.mkdirs() ;
        file = new File( dir, "props.txt");
        props = new Properties () ;
        if ( file.exists() ) {
            try {
                FileReader r = new FileReader(file);
                props.load( r );
                r.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getProperty (String key ) {
        return props.getProperty( key ) ;
    }

    public void setProperty (String key, String value ) {
        props.setProperty( key, value ) ;
        store () ;
    }

    private void store() {
        try {
            FileWriter w = new FileWriter(file);
            props.store( w, "MyPropertyFile");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearProperty(String key) {
        props.remove( key ) ;
        store();
    }

    public void setProperties(Properties p) {
        for ( Map.Entry e : p.entrySet() ) {
            props.setProperty( (String) e.getKey(), (String) e.getValue() ) ;
        }
        store();
    }
}
