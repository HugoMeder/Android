package de.bitsnarts.SocketAbstraction.TCPImpl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

import de.bitsnarts.SocketAbstraction.AbstractServerSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocket;
import de.bitsnarts.android.camera_snapshot2.Utils.Logger;

public class TCPServerSocket implements AbstractServerSocket {

	private ServerSocket ss;
	
	TCPServerSocket ( ServerSocket ss ) {
		this.ss = ss ;
	}
	@Override
	public AbstractSocket accept() throws IOException {
		Enumeration e = NetworkInterface.getNetworkInterfaces();
		while(e.hasMoreElements())
		{
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration ee = n.getInetAddresses();
			while (ee.hasMoreElements())
			{
				InetAddress i = (InetAddress) ee.nextElement();
				Logger.l(i.getHostAddress());
			}
		}
		Socket s = null ;
		try {
			s = ss.accept();
		} catch ( Throwable th ) {
			th.printStackTrace();
			throw th ;
		}
		return new TCPSocket ( s );
	}
	

}
