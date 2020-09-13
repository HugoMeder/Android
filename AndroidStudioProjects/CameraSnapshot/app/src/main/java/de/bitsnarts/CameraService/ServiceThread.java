package de.bitsnarts.CameraService;

import android.Manifest;
import android.support.v4.app.ActivityCompat;

import java.io.CharArrayWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;

import de.bitsnarts.BNASockets.BNALookup;
import de.bitsnarts.BNASockets.BNAServerSocket;

import static android.Manifest.permission.INTERNET;
import static android.support.v4.app.ActivityCompat.requestPermissions;

public class ServiceThread implements Runnable {

    private final ServiceImpl service;
    private boolean running = false ;
    private AppToClientProtImpl prot;

    ServiceThread ( ServiceImpl service ) {
        this.service = service ;
    }
    String exeptionToStr ( Throwable th ) {
        CharArrayWriter cs = new CharArrayWriter () ;
        try (PrintWriter pw = new PrintWriter(cs)) {
            th.printStackTrace( pw );
            pw.flush();
            return cs.toString () ;
        }
    }
    
    public void run () {
        InetAddress host = BNALookup.getAddress();
        int step = 0 ;
        service.setState ( "start thread" ) ;
        try {
            Socket s = new Socket(host, 99 ) ;
            for (int i = 0 ; i < 20 ; i++ ) {
                service.setState ( "step "+step++ ) ;
                Thread.sleep(1000 );
                boolean r ;
                synchronized ( this ) {
                    r = running ;
                }
                if ( !r ) {
                    break ;
                }
            }
            s.close();
            service.setState ( "thread stopped" ) ;
        } catch ( Throwable th ) {
            service.setState ( exeptionToStr( th ) ) ;
        }
    }
    public void run2() {

        service.setState ( "running" ) ;

        BNAServerSocket ss = new BNAServerSocket( 10 ) ;
        service.setState ( "server socket created" ) ;
        try {
            for (;;) {
                Socket s = null ;
                try {
                    s = ss.accept() ;
                    service.setState ( "connected" ) ;
                    int index = 0 ;
                    prot = new AppToClientProtImpl ( s ) ;
                    try {
                        Thread.sleep(1000 );
                        String str = "hello "+index++ +"\n" ;
                        service.setState ( str ) ;
                        prot.println( str );
                    /*
                    for (;;) {
                        } catch (IOException e) {
                            s.close();
                            service.setState ( "halted " ) ;
                            return ;
                        }
                    }
                    */
                        if ( !running ) {
                            s.close();
                            service.setState ( "halted " ) ;
                            return ;
                        }
                    } catch (IOException e) {
                        s.close();
                        service.setState ( "halted " ) ;
                        return ;
                    }
                    Thread.sleep ( 10000 ) ;
                    service.setState ( "exit thread" ) ;
                    s.close () ;
                    return ;
                } catch ( Exception e) {
                    service.setState ( exeptionToStr(e) ) ;
                    e.printStackTrace();
                    Thread.sleep( 10000 );
                    if ( s != null ) {
                        s.close () ;
                    }
                }
            }
        } catch ( Throwable th ) {
            service.setState ( "ended "+th.toString() ) ;
        }

    }

    public void halt () {
        synchronized ( this ) {
            running = false ;
        }

    }

    public boolean isRunning() {
        synchronized ( this ) {
            return running ;
        }
    }
}
