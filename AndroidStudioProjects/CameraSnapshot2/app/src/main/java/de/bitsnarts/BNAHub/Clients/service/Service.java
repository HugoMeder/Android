package de.bitsnarts.BNAHub.Clients.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;

import de.bitsnarts.BNAHub.Clients.BNAGlobals;

public class Service implements Runnable {

	private static Service service;
	private TreeMap<Integer,ServiceThread> services = new TreeMap<Integer,ServiceThread> () ;
	private static final int blen = 1024*1024 ;
	private ServerSocket ss;
	private boolean verbose;
	
	Service (boolean verbose ) {
		this.verbose = verbose ;
	}

	private void stop() {
		synchronized ( services ) {
			for ( ServiceThread s : services.values() ) {
				try {
					s.s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			if ( ss != null )
				ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	class ServiceThread implements Runnable {

		private Socket s;
		private Socket clientConnection;
		private int serviceID;

		public ServiceThread(Socket s, int serviceID) {
			this.s = s ;
			this.serviceID = serviceID ;
		}

		class TransferThread implements Runnable {

			private InputStream in;
			private OutputStream out;

			TransferThread ( InputStream in, OutputStream out ) {
				this.in = in ;
				this.out = out ;
			}
			
			@Override
			public void run() {
				byte[] buffer = new byte[blen] ;
				for (;;) {
					try {
						int n = in.read( buffer ) ;
						if ( n == -1 ) {
							break ;
						}
						out.write( buffer, 0, n );
						out.flush();
						//if ( verbose )
						//	System.out.println ( ""+n+" bytes transferred" ) ;
					} catch (IOException e) {
						break ;
					}
				}
				synchronized ( ServiceThread.this ) {
					try {
						s.close();
					} catch (IOException e1) {
					}
					try {
						clientConnection.close();
					} catch (IOException e1) {
					}							
					if ( verbose )
						System.out.println ( "service thread terminates for id "+serviceID ) ;
				}
			}			
		}
		
		@Override
		public void run() {
			try {
				new Thread ( new TransferThread ( s.getInputStream(), clientConnection.getOutputStream() ) ).start() ;
				TransferThread tt = new TransferThread ( clientConnection.getInputStream(), s.getOutputStream() ) ;
				tt.run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void setClientConnection(Socket s2) {
			synchronized ( this ) {
				this.clientConnection = s2 ;				
			}
		}

	}
	
	@Override
	public void run() {
		int port = BNAGlobals.port ;
		try {
			ss = new ServerSocket ( port );
			if ( verbose )
				System.out.println ( "service server socket established, listening on port "+port ) ;
			while ( true ) {
				if ( verbose )
					System.out.println ( "accept..." ) ;
				Socket s = ss.accept() ;
				s.setSoLinger( false, 0 );
				if ( verbose )
					System.out.println ( "call..." ) ;
				InputStream in = s.getInputStream () ;
				DataInputStream din = new DataInputStream ( in ) ;
				DataOutputStream dout = new DataOutputStream ( s.getOutputStream() ) ;
				boolean isService = din.readBoolean() ;
				int serviceID = din.readInt() ;
				if ( isService ) {
					ServiceThread old ;
					synchronized ( services ) {
						old = services.get( serviceID ) ;
					}
					if ( old != null ) {
						boolean skip = false ;
						try {
							old.s.getOutputStream().write ( 2 ) ;
						} catch ( IOException e ) {
							old.s = s ;
							dout.writeBoolean( true );
							dout.flush();
							skip = true ;
							if ( verbose )
								System.out.println ( "service reinstalled for id "+serviceID ) ;
						}
						if ( !skip ) {
							if ( verbose )
								System.out.println ( "service denied for id "+serviceID+" already running" ) ;
							dout.writeBoolean( false );
							dout.flush();
							s.close();							
						}
					} else {
						synchronized ( services ) {
							services.put( serviceID, new ServiceThread ( s, serviceID ) ) ;
						}
						if ( verbose )
							System.out.println ( "service installed for id "+serviceID ) ;
						dout.writeBoolean( true );
						dout.flush();
					}
				} else {
					ServiceThread old ;
					synchronized ( services ) {
						old = services.get( serviceID ) ;
					}
					if ( old == null ) {
						if ( verbose )
							System.out.println ( "client connection refused for "+serviceID+" service not installed" ) ;
						dout.writeBoolean( false );
						dout.flush();
						s.close();						
					} else {
						boolean skip = false ;
						try {
							old.s.getOutputStream().write ( 2 ) ;
						} catch ( IOException e ) {
							old.s = s ;
							dout.writeBoolean( false );
							dout.flush();
							old.s.close();
							synchronized ( services ) {
								services.remove( serviceID ) ;
							}							
							skip = true ;
							if ( verbose )
								System.out.println ( "client connection refused for "+serviceID+" service obsolete" ) ;
							return ; 
						}
						if ( !skip ) {
							synchronized ( services ) {
								services.remove( serviceID ) ;
							}
							old.setClientConnection( s );
							dout.writeBoolean( true );
							dout.flush();
							old.s.getOutputStream().write ( 1 ) ;
							new Thread ( old ).start();
							if ( verbose )
								System.out.println ( "client connection established for "+serviceID ) ;							
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void start ( String args[] ) {
		service = new Service ( true ) ;
		new Thread ( service ).start();
		System.out.println ( "Service up and running" ) ;		
	}
	
	public static void stop ( String args[] ) {
		if ( service != null ) {
			System.out.println ( "Stop... " ) ;				
			service.stop () ; 
			System.out.println ( "Stop... " ) ;				
		}
	}
	
	public static void main ( String args[] ) {
		start ( null ) ;
	}

	
}
