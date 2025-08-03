package de.bitsnarts.BNAHub.Camera.connectivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

class BroadcastThread implements Runnable {

	private List<Inet4AddressWithNetworkPrefix> la;
	private DatagramPacket[] dgs;
	
	public BroadcastThread() {
	}

	private DatagramPacket[] createDatagrams() {
		int n = la.size() ;
		DatagramPacket[] rv = new DatagramPacket[n] ;
		ByteArrayOutputStream out = new ByteArrayOutputStream () ;
		DataOutputStream dout = new DataOutputStream ( out ) ;
		for ( int i = 0 ; i < n ; i++ ) {
			try {
				dout.writeInt ( 1234 ) ;
				dout.writeInt( 1 );// version 
				/*
				dout.writeInt( la.size() );
				for (Inet4AddressWithNetworkPrefix a : la ) {
					dout.writeByte( a.networkPrefixLength );
					dout.writeUTF(a.addr.getHostAddress() );
				}*/
				dout.writeInt( 1 );
				Inet4AddressWithNetworkPrefix a = la.get(i) ;
				dout.writeByte( a.networkPrefixLength );
				dout.writeUTF(a.addr.getHostAddress() );
				dout.flush();
			} catch (IOException e) {
			}
			byte[] buffer = out.toByteArray() ;
			out.reset();
			rv[i] = new DatagramPacket(buffer, buffer.length, la.get(i).getBroadcstAddress(), 1024 );
		}
		return rv ;
	}

	@Override
	public void run() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			socket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
        }
        for ( ;; ) {
        	try {
				la = ConnectionFactory.getLocalAddress () ;
				dgs = createDatagrams () ;
				for ( DatagramPacket dg : dgs ) {
					socket.send(dg);
				}
				System.out.println ( "Sent" ) ;
			} catch (IOException e) {
				e.printStackTrace();
			}
        	try {
				Thread.sleep ( 1000 ) ;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
	}
	
}