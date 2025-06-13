package de.bitsnarts.SocketAbstraction;

import java.io.IOException;

public interface AbstractSocketFactory {
	AbstractSocket createClientSocket () throws IOException ;
	AbstractServerSocket createServerSocket () throws IOException ;
}
