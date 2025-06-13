package de.bitsnarts.SocketAbstraction.TCPImpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import de.bitsnarts.SocketAbstraction.AbstractServerSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocketFactory;

public class TCPSocketFactory implements AbstractSocketFactory {

	private InetAddress addr;
	private int port;
	
	public TCPSocketFactory ( InetAddress addr, int port ) {
		this.addr = addr ;
		this.port = port ;
	}
	
	public TCPSocketFactory ( String host, int port ) throws UnknownHostException {
		this.addr = InetAddress.getByName(host) ;
		this.port = port ;
	}
	
	@Override
	public AbstractSocket createClientSocket() throws IOException {
		return new TCPSocket ( new Socket ( addr, port ) ) ;
	}
	
	@Override
	public AbstractServerSocket createServerSocket() throws IOException {
		ServerSocket ss = new ServerSocket ( port ) ;
		return new TCPServerSocket ( ss );
	}

}
