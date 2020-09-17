package inducesmile.com.camera2;

import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;

import inducesmile.com.camera2.CameraTasks;
import inducesmile.com.camera2.ServiceToClientCmds;
import inducesmile.communication.BNALookup;
import inducesmile.communication.BNAServerSocket;

public class CommunicationThread implements Runnable {

    private final CameraTasks service;
    private boolean running = false ;
    private boolean ended;
    private BNAServerSocket ss;
    private Socket s ;
    private Vector<String> printlns = new Vector<String> () ;
    private Object sendMonitor = new Object () ;
    private boolean sendData ;

    class SendThread implements Runnable {

        @Override
        public void run() {
            DataOutputStream dout = null;
            try {
                dout = new DataOutputStream( s.getOutputStream() );
                for(;;) {
                    synchronized ( sendMonitor ) {
                        while ( !sendData ) {
                            try {
                                sendMonitor.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                        for ( String s : printlns ) {
                            try {
                                dout.writeInt ( ServiceToClientCmds.PRINTLN.getCode() ) ;
                                dout.writeUTF ( s ) ;
                            } catch ( Throwable e) {
                                log( e.toString() );
                            }
                        }
                        printlns.clear();
                        sendData = false ;
                        synchronized ( CommunicationThread.this ) {
                            if ( !running )
                                return ;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void log(String s) {
        service.log( "communication: "+s);
    }

    CommunicationThread(CameraTasks service ) { 
        this.service = service ;
        new Thread ( this ).start();
    }

    String exeptionToStr ( Throwable th ) {
        CharArrayWriter cs = new CharArrayWriter () ;
        try (PrintWriter pw = new PrintWriter(cs)) {
            th.printStackTrace( pw );
            pw.flush();
            return cs.toString () ;
        }
    }

    void notifySend () {
        synchronized ( sendMonitor ) {
            sendData = true ;
            sendMonitor.notifyAll();
        }
    }

    public void run0 () {
        InetAddress host = BNALookup.getAddress();
        int step = 0 ;
        service.log ( "start thread" ) ;
        try {
            Socket s = new Socket(host, 99 ) ;
            for (int i = 0 ; i < 20 ; i++ ) {
                service.log ( "step "+step++ ) ;
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
            service.log ( "thread stopped" ) ;
        } catch ( Throwable th ) {
            service.log ( exeptionToStr( th ) ) ;
        }
    }
    public void run() {

        synchronized ( this ) {
            running = true;
            ended = false;
        }
        service.log ( "running" ) ;
        ss = new BNAServerSocket( 10 ) ;
        service.log ( "server socket created" ) ;
        try {
            for (;;) {
                try {
                    s = ss.accept() ;
                    log ( "connected" ) ;
                    int index = 0 ;
                    new Thread ( new SendThread() ).start() ;
                    notifySend () ;
                    DataInputStream in = new DataInputStream(s.getInputStream());
                    for (;;) {
                        int cmd = in.readInt () ;
                        log( "cmd="+cmd ) ;
                        if ( !running ) {
                            s.close();
                            log("halted ");
                            return;
                        }
                    }
                } catch ( Exception e) {
                    log ( exeptionToStr(e) ) ;
                    e.printStackTrace();
                    Thread.sleep( 10000 );
                    if ( s != null ) {
                        s.close () ;
                    }
                }
            }
        } catch ( Throwable th ) {
            synchronized ( this ) {
                running = false;
                ended = true ;
                notifyAll () ;
            }
            log ( "ended "+th.toString() ) ;
        }

    }

    public void stopCommunication () {
        log ( "stopCommunication" ) ;
        synchronized ( this ) {
            running = false ;

            /*
            if ( ss != null ) {
                ss.close();
            }
            if ( s != null ) {
                try {
                    s.close () ;
                } catch ( Exception e ) {
                }
            }
            service.setState ( "wait" ) ;
            while ( !ended ) {
                try {
                    wait ();
                } catch (InterruptedException e) {
                }
            }
            */
        }
        synchronized ( sendMonitor ) {
            sendData = true ;
            sendMonitor.notifyAll();
        }
    }

    public boolean isRunning() {
        synchronized ( this ) {
            return running ;
        }
    }

    public void startCommunication() {
        new Thread ( this ).start();
    }

    void println ( String text ) {
        if ( text == null )
            return ;
        synchronized ( sendMonitor ) {
            printlns.add( text ) ;
            notifySend() ;
        }
    }
}
