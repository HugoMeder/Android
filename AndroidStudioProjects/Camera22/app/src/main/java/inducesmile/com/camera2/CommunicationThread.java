package inducesmile.com.camera2;

import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.DataOutput;
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
    private Vector<OutMessage> outQueue = new Vector<OutMessage> () ;
    private Object sendMonitor = new Object () ;
    private int previewFramesInQueue ;
    private int imageFramesInQueue ;

    abstract class OutMessage {
        abstract void write (DataOutput out ) throws IOException;
    }

    class Println extends OutMessage {
        String line ;
        Println ( String line ) {
            this.line = line ;
        }

        @Override
        void write(DataOutput dout) throws IOException {
            dout.writeInt ( ServiceToClientCmds.PRINTLN.getCode() ) ;
            dout.writeUTF ( line ) ;
        }
    }

    class Preview extends OutMessage {


        private final FrameBufferQueue.JPegFrameBuffer buf;

        public Preview(FrameBufferQueue.JPegFrameBuffer buf) {
            this.buf = buf ;
        }

        @Override
        void write(DataOutput dout) throws IOException {
            dout.writeInt ( ServiceToClientCmds.PREVIEW.getCode() ) ;
            dout.writeInt( buf.getWidht() );
            dout.writeInt( buf.getHeight() );
            dout.writeInt( buf.getBufferSizes() );
            dout.write ( buf.getBuffer(), 0, buf.getBufferSizes() ) ;
            buf.release();
            synchronized ( outQueue ) {
                previewFramesInQueue-- ;
            }
        }
    }

    class Image extends OutMessage {


        private final FrameBufferQueue.JPegFrameBuffer buf;

        public Image(FrameBufferQueue.JPegFrameBuffer buf) {
            this.buf = buf ;
        }

        @Override
        void write(DataOutput dout) throws IOException {
            dout.writeInt ( ServiceToClientCmds.IMAGE.getCode() ) ;
            dout.writeInt( buf.getWidht() );
            dout.writeInt( buf.getHeight() );
            dout.writeInt( buf.getBufferSizes() );
            dout.write ( buf.getBuffer(), 0, buf.getBufferSizes() ) ;
            buf.release();
            synchronized ( outQueue ) {
                imageFramesInQueue-- ;
            }
        }
    }


    class SendThread implements Runnable {

        @Override
        public void run() {
            DataOutputStream dout = null;
            try {
                dout = new DataOutputStream( s.getOutputStream() );
                for(;;) {
                    OutMessage msg ;
                    synchronized ( sendMonitor ) {
                        while ( outQueue.size() == 0 ) {
                            try {
                                sendMonitor.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                        msg = outQueue.get ( 0 ) ;
                        outQueue.removeElementAt( 0 );
                        synchronized ( CommunicationThread.this ) {
                            if ( !running )
                                return ;
                        }
                    }
                    msg.write( dout );
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
                        if ( cmd == 1 )
                            service.takePicture();

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
        Println pl = new Println ( text ) ;
        synchronized ( sendMonitor ) {
            outQueue.add( pl ) ;
            notifySend() ;
        }
    }

    public void previewImage(FrameBufferQueue.JPegFrameBuffer buf) {
        Preview pw = new Preview ( buf ) ;
        synchronized ( sendMonitor ) {
            if ( previewFramesInQueue > 10 ) {
                buf.release();
                return;
            }
            previewFramesInQueue++ ;
            outQueue.add( pw ) ;
            notifySend() ;
        }
    }

    public void image(FrameBufferQueue.JPegFrameBuffer buf) {
        Image img = new Image ( buf ) ;
        synchronized ( sendMonitor ) {
            if ( imageFramesInQueue > 1 ) {
                buf.release();
                return;
            }
            imageFramesInQueue++ ;
            outQueue.add( img ) ;
            notifySend() ;
        }
    }


    void flush () {
        synchronized ( outQueue ) {
            while ( outQueue.size() != 0 ) {
                try {
                    outQueue.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
