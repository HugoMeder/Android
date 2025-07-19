package de.bitsnarts.BNAHub.Camera.connectivity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BroadcastReceiveThread implements Runnable {

	private Inet4AddressWithNetworkPrefix[] cameraAddress;
	
	BroadcastReceiveThread () {
	}
	
	Inet4AddressWithNetworkPrefix[] getCameraAddress () {
		synchronized ( this ) {
			while ( cameraAddress == null ) {
				try {
					this.wait( 1000 );
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return cameraAddress ;
		}
	}
	
	@Override
	public void run() {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(1024);
		} catch (IOException e) {
			e.printStackTrace();
		}
		DatagramPacket packet;
		for (;;) {
		    byte[] buf = new byte[1024];
		    packet = new DatagramPacket(buf, buf.length);
		    try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		    byte[] data = packet.getData() ;
		    ByteArrayInputStream in = new ByteArrayInputStream ( data ) ;
		    DataInputStream din = new DataInputStream ( in ) ;
		    try {
				int key = din.readInt() ;
				int vers = din.readInt() ;
				int n = din.readInt() ;
				Inet4AddressWithNetworkPrefix[] addrs = new Inet4AddressWithNetworkPrefix[n] ;
				for ( int j = 0 ; j < n ; j++ ) {
					int npl = din.readByte() ;
					Inet4Address addr = (Inet4Address)InetAddress.getByName( din.readUTF() ) ;
					addrs[j] = new Inet4AddressWithNetworkPrefix ( npl, addr ) ;
				}
				synchronized ( this ) {
					cameraAddress = addrs ;
					notifyAll();
					break ;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		socket.close();
	}

}
