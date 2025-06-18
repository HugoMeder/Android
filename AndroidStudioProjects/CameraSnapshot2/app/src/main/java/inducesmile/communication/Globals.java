package inducesmile.communication;

import java.net.UnknownHostException;

import de.bitsnarts.BNAHub.Clients.BNASocketFactory;
import de.bitsnarts.SocketAbstraction.AbstractSocketFactory;
import de.bitsnarts.SocketAbstraction.TCPImpl.TCPSocketFactory;

public class Globals {

    static private AbstractSocketFactory factory = new BNASocketFactory( 10 ) ;
    static private AbstractSocketFactory factory2;

    static {
        try {
            factory2 = new TCPSocketFactory( "192.168.0.184", 8888 );
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static AbstractSocketFactory getSocketFactory () {
        return factory2 ;
    }
}
