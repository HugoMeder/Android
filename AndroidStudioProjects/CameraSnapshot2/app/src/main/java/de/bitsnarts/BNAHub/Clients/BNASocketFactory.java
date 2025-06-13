package de.bitsnarts.BNAHub.Clients;

import java.io.IOException;

import de.bitsnarts.SocketAbstraction.AbstractServerSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocket;
import de.bitsnarts.SocketAbstraction.AbstractSocketFactory;

public class BNASocketFactory implements AbstractSocketFactory {

	private int serviceID;

	public BNASocketFactory ( int serviceID ) {
		this.serviceID = serviceID ;
	}
	
	@Override
	public AbstractSocket createClientSocket() throws IOException {
		return new BNAClientSocket ( serviceID );
	}

	@Override
	public AbstractServerSocket createServerSocket() throws IOException {
		return new BNAServerSocket ( serviceID );
	}

}
