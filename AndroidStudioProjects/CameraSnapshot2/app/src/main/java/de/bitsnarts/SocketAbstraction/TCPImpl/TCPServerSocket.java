package de.bitsnarts.SocketAbstraction.TCPImpl;

import java.io.IOException;
import java.net.ServerSocket;

import de.bitsnarts.SocketAbstraction.AbstractServerSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocket;

public class TCPServerSocket implements AbstractServerSocket {

	private ServerSocket ss;
	
	TCPServerSocket ( ServerSocket ss ) {
		this.ss = ss ;
	}
	@Override
	public AbstractSocket accept() throws IOException {
		return new TCPSocket ( ss.accept() );
	}
	

}
