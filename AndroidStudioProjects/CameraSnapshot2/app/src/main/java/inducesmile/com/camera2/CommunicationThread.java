package inducesmile.com.camera2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

import de.bitsnarts.SocketAbstraction.AbstractServerSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocket;
import inducesmile.communication.Globals;
import inducesmile.communication.LogUtils;

import androidx.appcompat.app.AppCompatActivity;

public class CommunicationThread implements Runnable {

    private final CameraTasks service;
    private boolean running = false ;
    private boolean ended;
    private AbstractServerSocket ss;
    private AbstractSocket s ;
    private Vector<OutMessage> outQueue = new Vector<OutMessage> () ;
    private Object sendMonitor = new Object () ;
    private int previewFramesInQueue ;
    private int previewFramesSent ;
    private int imageFramesInQueue ;
    private Context context;
    private int lastPWF_nr_acked = -1;
    private long lastPWF_timestamp = 0 ;
    public void setContext(AppCompatActivity context) {
        synchronized ( this ) {
            this.context = context.getApplicationContext();
        }
    }

    void setAckData ( int nr, long ts ) {
        synchronized ( this ) {
            this.lastPWF_nr_acked = nr ;
            this.lastPWF_timestamp = ts ;
        }

    }
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


        private final FrameBufferQueue.FrameBuffer buf;

        public Preview(FrameBufferQueue.FrameBuffer buf) {
            this.buf = buf ;
        }

        @Override
        void write(DataOutput dout) throws IOException {
            dout.writeInt ( ServiceToClientCmds.PREVIEW.getCode() ) ;
            dout.writeInt ( buf.getFormat() ) ;
            dout.writeInt ( buf.getOrientation() ) ;
            dout.writeInt( buf.getWidht() );
            dout.writeInt( buf.getHeight() );
            dout.writeInt( buf.getBufferSizes() );
            dout.write ( buf.getBuffer(), 0, buf.getBufferSizes() ) ;
            synchronized ( this ) {
                dout.writeInt(lastPWF_nr_acked);
                dout.writeLong(lastPWF_timestamp);
            }
            buf.release();
            synchronized ( outQueue ) {
                previewFramesInQueue-- ;
                previewFramesSent++ ;
            }
        }
    }

    class Image extends OutMessage {


        private final FrameBufferQueue.FrameBuffer buf;

        public Image(FrameBufferQueue.FrameBuffer buf) {
            this.buf = buf ;
        }

        @Override
        void write(DataOutput dout) throws IOException {
            dout.writeInt ( ServiceToClientCmds.IMAGE.getCode() ) ;
            dout.writeInt ( buf.getFormat() ) ;
            dout.writeInt ( buf.getOrientation() ) ;
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

    boolean onWLAN () {
        if ( true ) {
            return true;
        }
        Context a ;
        synchronized ( this ) {
            a = context;;
        }
        if ( a == null ) {
            log ( "no context" ) ;
            return false;
        }
        /*
        android.net.wifi.WifiManager m = (WifiManager) a.getSystemService(WIFI_SERVICE);
        android.net.wifi.SupplicantState s = m.getConnectionInfo().getSupplicantState();
        NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(s);
        boolean rv = (state == NetworkInfo.DetailedState.CONNECTED) ;
         */
        boolean rv = isConnectedWifi1 ( a ) ;
        if ( !rv ) {
            log("no WLAN " );
        }
        return rv ;
    }

    public boolean isConnectedWifi1(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
                for (NetworkInfo ni : netInfo) {
                    if ((ni.getTypeName().equalsIgnoreCase("WIFI"))
                            && ni.isConnected()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log (LogUtils.exeptionToStr( e ) ) ;
        }
        return false;
    }
    public void run() {

        synchronized ( this ) {
            running = true;
            ended = false;
        }
        service.log ( "running" ) ;
        //ss = new BNAServerSocket( 10 ) ;
        try {
            ss = Globals.getSocketFactory().createServerSocket() ;
        } catch (IOException e) {
            service.log( e.toString() );
            return ;
        }
        service.log ( "server socket created" ) ;
        try {
            for (;;) {
                try {
                    if ( !onWLAN() ) {
                        throw new IOException ( "not on wlan" ) ;
                    }
                    s = ss.accept() ;
                    log ( "connected" ) ;
                    int index = 0 ;
                    previewFramesSent = 0 ;
                    new Thread ( new SendThread() ).start() ;
                    notifySend () ;
                    DataInputStream in = new DataInputStream(s.getInputStream());
                    for (;;) {
                        if ( !onWLAN() ) {
                            throw new IOException ( "not on wlan" ) ;
                        }
                        int cmd = in.readInt () ;
                        log( "cmd="+cmd ) ;
                        if ( cmd == 1 )
                            service.takePicture();
                        else if ( cmd == 2 ){
                            int pwf = in.readInt() ;
                            long pwf_timestamp = in.readLong();
                            setAckData( pwf, pwf_timestamp );
                        }
                        if ( !running ) {
                            s.close();
                            log("halted ");
                            return;
                        }
                    }
                } catch ( Exception e) {
                    log ( exeptionToStr(e) ) ;
                    e.printStackTrace();
                    Thread.sleep( 1000 );
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

    public void previewImage(FrameBufferQueue.FrameBuffer buf) {
        Preview pw = new Preview ( buf ) ;
        synchronized ( sendMonitor ) {
            if ( previewFramesInQueue > 2 || previewFramesSent-lastPWF_nr_acked > 10 ) {
                buf.release();
                return;
            }
            previewFramesInQueue++ ;
            outQueue.add( pw ) ;
            notifySend() ;
        }
    }

    public void image(FrameBufferQueue.FrameBuffer buf) {
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
