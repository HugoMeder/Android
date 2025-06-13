package de.bitsnarts.BNAHub.Clients;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

class BNAClientSocket extends BNASocket {

	BNAClientSocket ( int seviceID ) throws IOException {
		super ( createSocket ( seviceID ) ) ;
	}
	
	private static Socket createSocket(int seviceID) throws IOException {
		InetAddress server = BNALookup.getAddress () ;
		Socket s = new Socket ( server, BNAGlobals.port ) ;
		DataOutputStream dout = new DataOutputStream ( s.getOutputStream() ) ;
		dout.writeBoolean( false );
		dout.writeInt( seviceID );
		dout.flush();
		int b = s.getInputStream().read () ;
		if ( b != 1 )
			throw new IOException ( "connection refused by BNA service" ) ;
		return s ;
	}

}
