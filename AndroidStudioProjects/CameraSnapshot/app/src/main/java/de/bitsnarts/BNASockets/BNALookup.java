package de.bitsnarts.BNASockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class BNALookup {

	private InetAddress address;

	BNALookup () throws IOException {
		URL url = new URL ( "http://www.utevonheubach.de/dyndns/dyndns.php?pass=Passwort" ) ;
		HttpURLConnection con = (HttpURLConnection) url.openConnection() ;
		InputStream in = con.getInputStream() ;
		InputStreamReader rdr = new InputStreamReader ( in ) ;
		LineNumberReader linp = new LineNumberReader ( rdr ) ;
		String line = linp.readLine() ;
		int index = 0 ;
		while ( line != null ) {
			//System.out.println ( line ) ;
			if ( index == 1 ) {
				interprete ( line ) ;
				break ;
			}
			index++ ;
			line = linp.readLine() ;
		}
		linp.close();
			
		//address = InetAddress.getByName( "127.0.0.1" ) ;
	}
	
	private void interprete(String line) throws UnknownHostException {
		/*byte[] bytes = new byte[4] ;
		StringTokenizer tok = new StringTokenizer ( line , "." ) ;
		if ( tok.countTokens() != 4 )
			return ;
		for ( int i = 0 ; i < 4 ; i++ ) {
			bytes[i] = (byte) Integer.parseInt( tok.nextToken() ) ;
		}
		address = InetAddress.getByAddress( bytes ) ;
		*/
		address = InetAddress.getByName( line ) ;
	}

	public static InetAddress getAddress () {
		BNALookup lba;
		try {
			lba = new BNALookup ();
		} catch (IOException e) {
			return null ;
		}
		return lba.address ;
		
	}
	public static void main ( String args[] ) throws IOException {
		System.out.println ( getAddress().toString() ) ;
	}


}
