package de.bitsnarts.BNAHub.Camera.connectivity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastReceiveThread implements Runnable {

	private InetAddress group;
	private int multicastPort;
	private String cameraAddress;
	
	MulticastReceiveThread ( InetAddress group, int multicastPort ) {
		this.group = group ;
		this.multicastPort = multicastPort ;
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
		

		MulticastSocket socket = null;
		try {
			socket = new MulticastSocket(multicastPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			socket.joinGroup(group);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DatagramPacket packet;
		for (;;) {
		    byte[] buf = new byte[1024];
		    packet = new DatagramPacket(buf, buf.length);
		    try {
				socket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    System.out.println ( "Recieved!" ) ;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			socket.leaveGroup(group);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket.close();
	}

}
