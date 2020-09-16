package inducesmile.communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class BNAPrintlnService {

    private Vector<String> lines = new Vector<String> () ;
    private String state = "initial" ;
    private SimpleDateFormat df = new SimpleDateFormat( "dd.MM.yy HH:mm:ss.SSSS") ;

    class WorkerThread implements Runnable {

        @Override
        public void run() {
            setState ( "running" ) ;
            for(;;) {
                InetAddress addr = BNALookup.getAddress();
                setState ( "BNA address = "+addr.toString() ) ;
                if ( addr == null ) {
                    try {
                        Thread.sleep( 1000 );
                    } catch (InterruptedException e) {
                        break ;
                    }
                    continue;
                }
                Socket s = null ;
                try {
                    s = new Socket(addr, 99);
                    DataOutputStream dout = new DataOutputStream(s.getOutputStream());;
                    setState ( "connected" ) ;
                    for(;;) {
                        synchronized ( lines ) {
                            while ( lines.size() == 0 ) {
                                lines.wait();
                            }
                            for ( String l : lines ) {
                                dout.writeUTF ( l ) ;
                            }
                            lines.clear();
                        }
                    }
                } catch (Throwable e) {
                    if ( s != null ) {
                        try {
                            s.close () ;
                            setState ( "closed" ) ;
                        } catch (IOException e1) {
                        }
                    }
                    try {
                        Thread.sleep( 1000 );
                    } catch (InterruptedException e2) {
                        break ;
                    }
                    continue;
                }
            }
            setState ( "exit" ) ;
        }

    }

    private void setState(String state ) {
        synchronized ( this ) {
            this.state = state;
        }
    }

    public String getState() {
        synchronized ( this ) {
            return state ;
        }
    }

    public BNAPrintlnService() {
        new Thread ( new WorkerThread () ).start();
    }

    public void println ( String line ) {
        Date date = new Date();;
        synchronized ( lines ) {
            lines.add ( df.format( date )+" "+line ) ;
            if ( lines.size() == 1 )
                lines.notify();
        }
    }
}
