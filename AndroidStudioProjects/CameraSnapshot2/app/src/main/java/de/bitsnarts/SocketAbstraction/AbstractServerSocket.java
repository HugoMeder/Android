package de.bitsnarts.SocketAbstraction;

import java.io.IOException;

public interface AbstractServerSocket {
	AbstractSocket accept () throws IOException ;
}
