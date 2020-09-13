package de.bitsnarts.CameraService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import de.bitsnarts.Camera.Protocol.AppToClient.InCmds;
import de.bitsnarts.Camera.Protocol.AppToClient.ProtocolAppToClient;

public class AppToClientProtImpl {

    private final Socket s;
    private DataOutputStream dout;

    AppToClientProtImpl (Socket s ) throws IOException {
        this.s = s ;
        dout = new DataOutputStream ( s.getOutputStream () ) ;
    }

    public void println( String str ) throws IOException {
        dout.writeInt(InCmds.PRINTLN.getCode() );
        dout.writeUTF( str );
        dout.flush();
    }
}
