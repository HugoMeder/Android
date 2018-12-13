package de.bitsnarts.android.tools.logger;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Logger {

    static PrintStream logger ;
    private static NetStreamThread out;

    public static PrintStream log() {

        if ( logger != null )
            return logger ;
        out = new NetStreamThread("192.168.178.36"/*"DESKTOP-48JFS75"*/, 1000);;
        logger = new PrintStream ( out, true ) ;
        return logger ;
    }


    public static void main ( String args[] ) {
        InetAddress addr = null;
        try {
            addr = Inet4Address.getByName("DESKTOP-48JFS75");
            System.out.println ( addr.toString()) ;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ;
    }

    public static void finish() {
        if ( out != null ) {
            logger.println ( "closed!" ) ;
            try {
                out.close();
            } catch (IOException e) {
            }
            out = null ;
        }
    }
}
