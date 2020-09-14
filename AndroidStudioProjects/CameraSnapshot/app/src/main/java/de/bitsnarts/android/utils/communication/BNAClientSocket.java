package de.bitsnarts.android.utils.communication;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import de.bitsnarts.BNASockets.Globals;

public class BNAClientSocket implements Closeable {

	private Socket s;

	public BNAClientSocket ( int seviceID ) throws IOException {
		InetAddress server = BNALookup.getAddress () ;
		s = new Socket ( server, Globals.port ) ;
		DataOutputStream dout = new DataOutputStream ( s.getOutputStream() ) ;
		dout.writeBoolean( false );
		dout.writeInt( seviceID );
		dout.flush();
		int b = s.getInputStream().read () ;
		if ( b != 1 )
			throw new IOException ( "connection refused by BNA service" ) ;
	}
	
	public InputStream getInputStream () throws IOException {
		return s.getInputStream() ;
	}
	
	public OutputStream getOutputStream () throws IOException {
		return s.getOutputStream() ;
	}
	
	public void close () throws IOException {
		s.close();
	}

	public Socket getInetSocket() {
		return s;
	}
}
