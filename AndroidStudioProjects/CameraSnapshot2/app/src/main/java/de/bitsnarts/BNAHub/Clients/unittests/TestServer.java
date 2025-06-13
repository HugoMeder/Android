package de.bitsnarts.BNAHub.Clients.unittests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import de.bitsnarts.BNAHub.Clients.BNASocketFactory;
import de.bitsnarts.SocketAbstraction.AbstractServerSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocket;

public class TestServer {

	private AbstractServerSocket ss;

	class EchoService {
		EchoService ( AbstractSocket s ) throws IOException {
			InputStream in = s.getInputStream() ;
			OutputStream out = s.getOutputStream() ;
			int blen = 1024 ;
			byte[] buffer = new byte[blen] ;
			for(;;) {
				int n = in.read ( buffer ) ;
				if ( n == -1 ) {
					s.close();
					return ;
				}
				out.write ( buffer, 0, n ) ; 
			}
		}
	}
	
	TestServer () {
		BNASocketFactory sf = new BNASocketFactory ( 2 ) ;
		try {
			ss = sf.createServerSocket() ;
		} catch (IOException e2) {
			e2.printStackTrace();
			return ;
		}
		for(;;) {
			try {
				System.out.println ( "listen" ) ;
				AbstractSocket s = ss.accept() ;
				new EchoService ( s ) ;
			} catch (IOException e) {
				e.printStackTrace();
				try {
					Thread.sleep( 1000 );
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}			
		}
	}
	
public static void main ( String args[] ) {
	new TestServer () ;
	}
}
