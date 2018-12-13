package de.bitsnarts.android.tools.logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class NetStreamThread extends OutputStream implements Runnable {

    private static Socket s;
    private final String host;
    private final int port;

    private ByteArrayOutputStream buffer = new ByteArrayOutputStream () ;
    private OutputStream out;

    NetStreamThread ( String host, int port ) {
        this.host = host ;
        this.port = port ;
        new Thread ( this ).start();
    }

    @Override
    public void write(int b) throws IOException {
        synchronized (buffer) {
            buffer.write(b);
            if ( b == '\n' )
                buffer.notify();
        }
    }

    private static OutputStream getOutputStream(String host, int port ) {
        try {
            s = new Socket ( host, port ) ;
            return s.getOutputStream() ;
        } catch (Throwable e) {
            e.printStackTrace();
            return new DummyStream () ;
        }
    }

    @Override

    public void close () throws IOException {
        if ( s != null )
            s.close();
    }

    @Override
    public void run() {
        this.out = getOutputStream ( host, port ) ;
        synchronized ( buffer ) {
            buffer.notify();
        }
        for (;;) {
            byte[] ba = null ;
            synchronized (buffer) {
                while (buffer.size() == 0) {
                    try {
                        buffer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ba = buffer.toByteArray();
                buffer.reset();
            }
            try {
                out.write( ba );
            } catch (IOException e) {
                synchronized (buffer) {
                    out = new DummyStream () ;
                }
            }
        }
    }
}
