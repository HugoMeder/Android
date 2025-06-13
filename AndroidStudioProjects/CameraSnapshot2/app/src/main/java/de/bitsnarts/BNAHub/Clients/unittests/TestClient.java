package de.bitsnarts.BNAHub.Clients.unittests;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import de.bitsnarts.BNAHub.Clients.BNASocketFactory;
import de.bitsnarts.SocketAbstraction.AbstractSocket;

public class TestClient {

	class ReceiveThead implements Runnable {
		private InputStream in;
		private AbstractSocket s;

		ReceiveThead ( AbstractSocket s2 ) throws IOException {
			this.in = s2.getInputStream() ;
			this.s = s2 ;
		}

		@Override
		public void run() {
			InputStreamReader rdr = new InputStreamReader ( in ) ;
			LineNumberReader linp = new LineNumberReader ( rdr ) ;
			try {
				String line = linp.readLine() ;
				while ( line != null ) {
					System.out.println ( line ) ;
					line = linp.readLine() ;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	TestClient () {
		AbstractSocket s = null ;
		try {
			BNASocketFactory sf = new BNASocketFactory ( 2 ) ;
			s = sf.createClientSocket() ;
			AbstractSocket is = s ;
			//is.setSoTimeout( 1000 );
			new Thread ( new ReceiveThead ( s ) ).start() ;
			int index = 1 ;
			OutputStreamWriter wrt = new OutputStreamWriter ( s.getOutputStream() ) ;
			for (;;) {
				String str = "line "+index++ ;
				wrt.write( str+"\n" ) ;
				wrt.flush();
				System.out.println ( "write "+str ) ;
				Thread.sleep( 1000 );
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				s.close () ;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return ;
		} catch (InterruptedException e) {
			e.printStackTrace();
			try {
				s.close () ;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return ;
		}
	}
	
	public static void main ( String args[] ) {
		new TestClient () ;
	}
}
