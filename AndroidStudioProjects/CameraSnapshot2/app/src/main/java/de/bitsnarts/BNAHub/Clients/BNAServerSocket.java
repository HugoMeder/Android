package de.bitsnarts.BNAHub.Clients;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import de.bitsnarts.SocketAbstraction.AbstractServerSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocket;

class BNAServerSocket implements AbstractServerSocket {

	private int serviceID;
	private Socket s;

	BNAServerSocket ( int serviceID ) {
		this.serviceID = serviceID ;
	}
	
	public void close () {
		if ( s!= null ) {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public AbstractSocket accept () throws IOException {
		InetAddress server = BNALookup.getAddress() ;
		s = new Socket ( server, BNAGlobals.port ) ;
		int to = s.getSoTimeout() ;
		s.setSoTimeout( 0 );
		DataOutputStream dout = new DataOutputStream ( s.getOutputStream() ) ;
		dout.writeBoolean( true );
		dout.writeInt( serviceID );
		dout.flush();
		DataInputStream din = new DataInputStream ( s.getInputStream() ) ;
		byte b = din.readByte() ;
		if ( b == 1 ) {
			b = din.readByte() ;
			while ( b == 2 ) {
				b = din.readByte() ;
			}
			if ( b == 1 ) {
				s.setSoTimeout( to );
				return new BNASocket ( s ) ;
			}
		}
		s.close();
		throw new IOException ( "connection refused by BNA server" ) ;
	}
	
	public static void main ( String args[] ) {
		BNAServerSocket ss = new BNAServerSocket ( 1 ) ;
		try {
			AbstractSocket s = ss.accept() ;
		} catch (IOException e) {
			e.printStackTrace();
			return ;
		}
		System.out.println ( "conntection to BNA server established" ) ;
	}
}
