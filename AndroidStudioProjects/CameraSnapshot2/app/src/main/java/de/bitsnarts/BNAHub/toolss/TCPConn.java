package de.bitsnarts.BNAHub.toolss;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class TCPConn {

	public static void main ( String args[] ) {
		try {
			Socket s = new Socket ( "localhost", 1111 ) ;
			OutputStream out = s.getOutputStream () ;
			DataOutputStream dout = new DataOutputStream ( out ) ;
			dout.writeBoolean( true ) ;
			dout.writeInt( 20 ) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
