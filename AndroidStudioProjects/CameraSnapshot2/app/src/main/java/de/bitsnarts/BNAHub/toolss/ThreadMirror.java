package de.bitsnarts.BNAHub.toolss;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadMirror {

	private ServerSocket ss;
	private int nextNr = 1 ;
	
	class Th implements Runnable {

		private Socket s;
		private int nr;
		
		Th ( Socket s ) {
			this.s = s ;
			this.nr = nextNr++ ;
		}
		@Override
		public void run() {
			System.out.println ( "Thread started "+nr) ;
			try {
				DataInputStream din = new DataInputStream ( s.getInputStream() ) ;
				for(;;) {
					String str = din.readUTF() ;
					System.out.println ( "thread "+nr+": "+str ) ;
				}
					
			} catch ( IOException e ) {
			}
			System.out.println ( "Thread terminated "+nr ) ;				
		}
		
	}
	
	ThreadMirror () {
		try {
			ss = new ServerSocket ( 99 );
			for ( ;; ) {
				Socket s = ss.accept() ;
				new Thread ( new Th ( s ) ).start() ;				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main ( String args[] ) {
		new ThreadMirror () ;
	}
}
