package de.bitsnarts.BNAHub.Camera.connectivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class BroadcastTest {

	private DatagramPacket createDatagram() {
		ByteArrayOutputStream out = new ByteArrayOutputStream () ;
		DataOutputStream dout = new DataOutputStream ( out ) ;
		try {
			dout.writeInt ( 1234 ) ;
			dout.writeInt( 0 );// version 
			dout.writeUTF(getLocalAddress());
			dout.flush();
		} catch (IOException e) {
		}
		byte[] buffer = out.toByteArray() ;
		InetAddress addr = null ;
		try {
			//addr = InetAddress.getByName ( "255.255.255.255" ) ;
			//addr = InetAddress.getByName ( "192.168.0.255" ) ;
			addr = InetAddress.getByName ( "192.168.0.161" ) ;
			//addr = InetAddress.getByName ( "192.168.0.184" ) ;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DatagramPacket dg = new DatagramPacket(buffer, buffer.length, addr, 1024 );
		return dg ;
	}

	class Sender implements Runnable { 

		@Override
		public void run() {
			try {
				DatagramSocket s = new DatagramSocket () ;
				DatagramPacket dg = createDatagram () ;
				//s.setBroadcast( true );
				for(;;) {
					s.send( dg );
					System.out.println ( "Sent" ) ;
					Thread.sleep( 10000 );
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	class Reciever implements Runnable {

		@Override
		public void run() {
			try {
				DatagramSocket s = new DatagramSocket ( 1024 ) ;
			    byte[] buf = new byte[1024];
			    DatagramPacket packet = new DatagramPacket(buf, buf.length);
			    for ( ;; ) {
			    	s.receive(packet);
			    	System.out.println ( "got it!" ) ;
			    }
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
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
	
	public BroadcastTest () {
		
		
	}
	
	public static void main ( String args[] ) {
		BroadcastTest bt = new BroadcastTest () ;
		//bt.startSender () ;
		bt.startReciever() ;
	}

	public void startReciever() {
		new Thread ( new Reciever() ).start();
	}

	public void startSender() {
		new Thread ( new Sender() ).start();
	}
}
