package inducesmile.communication;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public class LogUtils {

    public static String exeptionToStr ( Throwable th ) {
        CharArrayWriter cs = new CharArrayWriter () ;
        try (PrintWriter pw = new PrintWriter(cs)) {
            th.printStackTrace( pw );
            pw.flush();
            return cs.toString () ;
        }
    }


}
