package de.bitsnarts.android.camera_snapshot2.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Communication {

    public static void test(String txt) {
        Thread th = new Thread () {
            public void run () {
                try {
                    //Socket s = new Socket("192.168.178.125", 8888);
                    //Socket s = new Socket("87.123.129.136", 8888);
                    Socket s = new Socket("87.123.131.74", 8888);

                    OutputStream strm = s.getOutputStream();
                    DataOutputStream out = new DataOutputStream(strm);
                    out.writeUTF(txt);
                    out.close();
                    s.close();
                    Logger.l ( "done!" ) ;
                } catch ( Exception e) {
                    Logger.l ( ""+e ) ;
                }
            } ;
        } ;
        th.start();
    }
}
