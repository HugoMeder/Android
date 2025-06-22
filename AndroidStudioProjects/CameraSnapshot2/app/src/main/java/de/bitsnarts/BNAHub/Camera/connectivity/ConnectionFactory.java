package de.bitsnarts.BNAHub.Camera.connectivity;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;

//import javax.swing.JFrame;

import de.bitsnarts.BNAHub.Clients.BNASocketFactory;
import de.bitsnarts.SocketAbstraction.AbstractServerSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocketFactory;
import de.bitsnarts.SocketAbstraction.TCPImpl.TCPSocketFactory;

public class ConnectionFactory {

	private boolean asService;
	private AbstractSocketFactory socketFactory;
	private BroadcastReceiveThread receiveThread;
	private AbstractServerSocket serverSocket;
	private BroadcastThread broadcastThread;
	static InetAddress group = createGroup () ;
	static int multicastPort = 4446 ;
	public static final ConnectionFactory instance = createInstance () ;

	/*
	class Frame extends JFrame {
		private static final long serialVersionUID = -5686421863165129690L;

		Frame () {
			super ( "Warten auf Camera App" ) ;
			setSize ( 100, 100 ) ;
			this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
		}
	}
	*/
	ConnectionFactory ( boolean useBNAHub, boolean asService ) {
		if ( useBNAHub ) {
			socketFactory = new BNASocketFactory ( 10 ) ;
		} else {
			if ( asService ) {
				try {
					socketFactory = new TCPSocketFactory ( "127.0.0.1", 8888 ) ;
					startBroadcastThread () ;
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			} else {
				startReceiveThread () ;
				try {
					String addr = receiveThread.getCameraAddress() ;
					socketFactory = new TCPSocketFactory ( addr, 8888 ) ;
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}

				//startMulticastThread () ;
				//startRecieveThead () ;
				/*
				if ( runningOnPC () ) {
					//JOptionPane pane = new JOptionPane ( "Warten auf Camera App", JOptionPane.INFORMATION_MESSAGE ) ;
					//pane.setVisible( true );
					Frame f = new Frame () ;
					f.setVisible( true );
					String addr = receiveThread.getCameraAddress() ;
					System.out.println ( "camera address "+addr ) ;
					f.setVisible( false );
					try {
						socketFactory = new TCPSocketFactory ( addr, 8888 ) ;
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}*/
			}
		}
		this.asService = asService ;
		if ( asService ) {
			try {
				serverSocket = socketFactory.createServerSocket() ;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static ConnectionFactory createInstance() {
		if ( runningOnPC () ) {
			return new ConnectionFactory ( false, false ) ;
		} else {
			return new ConnectionFactory ( false, true ) ;
			
		}
	}

	private static InetAddress createGroup() {
		try {
			//return InetAddress.getByName("203.0.113.0");
			return InetAddress.getByName("224.0.0.1");
		} catch (UnknownHostException e) {
			throw new Error ( e ) ;
		}
	}

	private void startBroadcastThread() {
		String la = getLocalAddress () ;
		broadcastThread = new BroadcastThread ( la ) ;
		new Thread (broadcastThread).start();
	}

	private static String getLocalAddress() {
		Enumeration<NetworkInterface> e;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException ex) {
			ex.printStackTrace();
			return null ;
		}
		
		Inet4Address rv = null ;
		while(e.hasMoreElements())
		{
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration<InetAddress> ee = n.getInetAddresses();
			if ( ee.hasMoreElements() ) {
				String name = n.getName() ;
				boolean iswlan = name.indexOf( "wlan" ) == 0 ;
				if ( iswlan ) {
					System.out.println ( n.getDisplayName() ) ;
					System.out.println ( n.getName() ) ;
					
					while (ee.hasMoreElements())
					{
						InetAddress i = (InetAddress) ee.nextElement();
						if ( i instanceof Inet4Address ) {
							if ( rv != null )
								System.out.println ( "doppelte addresse" ) ;
							rv = (Inet4Address) i ;
						}
						System.out.println ( "\t"+i ) ;
					}
				}
			}
		}
		return rv.getHostAddress() ;
	}

	static String getBroadcastAddress () throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) 
		{
		    NetworkInterface networkInterface = interfaces.nextElement();
		    if (networkInterface.isLoopback())
		        continue;    // Do not want to use the loopback interface.
		    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) 
		    {
		        InetAddress broadcast = interfaceAddress.getBroadcast();
		        if (broadcast == null)
		            continue;
		        return broadcast.getHostAddress() ;
		    }
		}
		return null ;
	}
	
	static boolean runningOnPC () {
		Properties props = System.getProperties() ;
		String val = (String) props.get("os.name") ;
		if ( val == null )
			return false ;
		return val.indexOf ( "Windows" )!=-1 ;
	}
	
	public AbstractSocket getConnection () {
		for (;;) {
			try {
				AbstractSocket rv = null;
				if (asService) {
					rv = serverSocket.accept();
				} else {
					rv = socketFactory.createClientSocket();
				}
				if ( rv == null ) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						throw new RuntimeException(ex);
					}
				} else {
					return rv ;
				}
			} catch (IOException e) {
                try {
                    Thread.sleep( 1000 ) ;
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
		}
	}
	
	private void startReceiveThread() {
		
		receiveThread = new BroadcastReceiveThread ( ) ;
		new Thread ( receiveThread ).start() ;
	}
}