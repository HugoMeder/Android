package de.bitsnarts.BNAHub.Camera.connectivity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BroadcastReceiveThread implements Runnable {

	private String cameraAddress;
	
	BroadcastReceiveThread () {
	}
	
	String getCameraAddress () {
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
		for (int i = 0; i < 5; i++) {
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
				String addr = din.readUTF() ;
				synchronized ( this ) {
					cameraAddress = addr ;
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
