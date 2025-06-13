package de.bitsnarts.SocketAbstraction.TCPImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import de.bitsnarts.SocketAbstraction.AbstractSocket;

public class TCPSocket implements AbstractSocket {

	private Socket s;

	TCPSocket ( Socket s ) {
		this.s = s ;
	}
	
	@Override
	public void close() throws IOException {
		s.close () ;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return s.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return s.getOutputStream();
	}

}
