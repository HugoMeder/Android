package de.bitsnarts.CameraService;

import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;

import de.bitsnarts.android.utils.communication.BNALookup;
import de.bitsnarts.android.utils.communication.BNAServerSocket;

public class CommunicationThread implements Runnable {

    private final ServiceImpl service;
    private boolean running = false ;
    private AppToClientProtImpl prot;
    private boolean ended;
    private BNAServerSocket ss;
    private Socket s ;
    private Vector<String> printlns = new Vector<String> () ;
    private Object sendMonitor = new Object () ;
    private boolean sendData ;

    class SendThread implements Runnable {

        @Override
        public void run() {
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
                            prot.println( s );
                        } catch ( Throwable e) {
                            service.setState( e.toString() );
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
        }
    }

    CommunicationThread(ServiceImpl service ) {
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

    void notifySend () {
        synchronized ( sendMonitor ) {
            sendData = true ;
            sendMonitor.notifyAll();
        }
    }

    public void run0 () {
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
    public void run() {

        synchronized ( this ) {
            running = true;
            ended = false;
        }
        service.setState ( "running" ) ;
        ss = new BNAServerSocket( 10 ) ;
        service.setState ( "server socket created" ) ;
        try {
            for (;;) {
                try {
                    s = ss.accept() ;
                    service.setState ( "connected" ) ;
                    int index = 0 ;
                    prot = new AppToClientProtImpl ( s ) ;
                    new Thread ( new SendThread() ).start() ;
                    notifySend () ;
                    DataInputStream in = new DataInputStream(s.getInputStream());
                    for (;;) {
                        int cmd = in.readInt () ;
                        service.setState ( "cmd="+cmd ) ;
                        if ( !running ) {
                            s.close();
                            service.setState("halted ");
                            return;
                        }
                    }
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
            synchronized ( this ) {
                running = false;
                ended = true ;
                notifyAll () ;
            }
            service.setState ( "ended "+th.toString() ) ;
        }

    }

    public void stopCommunication () {
        service.setState ( "stopCommunication" ) ;
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
