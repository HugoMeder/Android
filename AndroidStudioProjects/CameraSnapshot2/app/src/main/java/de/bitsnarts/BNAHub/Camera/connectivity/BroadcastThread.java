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

class BroadcastThread implements Runnable {

	private String la;
	private DatagramPacket dg;
	
	public BroadcastThread(String la ) { 
		this.la = la ;
		dg = createDatagram () ;
		}

	private DatagramPacket createDatagram() {
		ByteArrayOutputStream out = new ByteArrayOutputStream () ;
		DataOutputStream dout = new DataOutputStream ( out ) ;
		try {
			dout.writeInt ( 1234 ) ;
			dout.writeInt( 0 );// version 
			dout.writeUTF(la);
			dout.flush();
		} catch (IOException e) {
		}
		byte[] buffer = out.toByteArray() ;
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName ( "255.255.255.255" );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		DatagramPacket dg = new DatagramPacket(buffer, buffer.length, addr, 1024 );
		return dg ;
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
				socket.send(dg);
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