package de.bitsnarts.BNAHub.Camera.connectivity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

class MulticastThread implements Runnable {

	private String la;
	private DatagramPacket dg;
	private InetAddress group;
	private int multicastPort;

	public MulticastThread(String la, InetAddress group, int port) { 
		this.la = la ;
		this.group = group ;
		this.multicastPort = port ; 
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
		DatagramPacket dg = new DatagramPacket(buffer, buffer.length, group, multicastPort );
		return dg ;
	}

	@Override
	public void run() {
		MulticastSocket socket = null;
		try {
			socket = new MulticastSocket();
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
				Thread.sleep ( 10000 ) ;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
	}
	
}