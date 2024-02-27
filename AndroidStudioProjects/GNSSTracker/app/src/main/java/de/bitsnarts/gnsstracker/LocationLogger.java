package de.bitsnarts.gnsstracker;

import android.location.Location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class LocationLogger {
    private final PrintWriter pw;

    public LocationLogger(File filesDir ) throws FileNotFoundException {
        File file = new File(filesDir, "locationLog.txt");
        FileOutputStream out = new FileOutputStream(file, true);
        pw = new PrintWriter( out, true ) ;
    }

    public void log(Location location) {
        pw.println ( location.getTime()+" "+location.getLongitude()+" "+location.getLatitude()+" "+location.getAltitude()+" "+location.getAccuracy() ) ;
    }
}
