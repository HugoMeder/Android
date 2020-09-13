package de.bitsnarts.BNASockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class BNAServerSocket {

	private int serviceID;
	private Socket s;

	public BNAServerSocket ( int serviceID ) {
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
	
	public Socket accept () throws IOException {
		InetAddress server = BNALookup.getAddress() ;
		s = new Socket ( server, Globals.port ) ;
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
				return s ;
			}
		}
		s.close();
		throw new IOException ( "connection refused by BNA server" ) ;
	}
	
	public static void main ( String args[] ) {
		BNAServerSocket ss = new BNAServerSocket ( 1 ) ;
		try {
			Socket s = ss.accept() ;
		} catch (IOException e) {
			e.printStackTrace();
			return ;
		}
		System.out.println ( "conntection to BNA server established" ) ;
	}
}
