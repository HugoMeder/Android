package inducesmile.communication;

import de.bitsnarts.BNAHub.Clients.BNASocketFactory;
import de.bitsnarts.SocketAbstraction.AbstractSocketFactory;

public class Globals {

    static private AbstractSocketFactory factory = new BNASocketFactory( 10 ) ;
    public static AbstractSocketFactory getSocketFactory () {
        return factory ;
    }
}
