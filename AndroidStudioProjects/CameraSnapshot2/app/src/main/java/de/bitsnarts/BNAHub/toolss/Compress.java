package de.bitsnarts.BNAHub.toolss;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Vector;

public class Compress {

	private File file;
	private Vector<String> lines;

	public Compress(File file) throws IOException {
		this.file = file ;
		read () ;
		write () ;
	}

	private void write() throws IOException {
		FileWriter w = new FileWriter ( file ) ;
		PrintWriter pw = new PrintWriter ( w ) ;
		for ( String l : lines ) {
			pw.println ( l ) ;
		}
		pw.close();
	}

	private void read() throws IOException {
		lines = new Vector<String> () ;
		FileReader rdr = new FileReader ( file ) ;
		LineNumberReader linp = new LineNumberReader ( rdr ) ;
		String line = linp.readLine() ;
		while ( line != null ) {
			line = line.trim() ;
			if ( line.length() > 0 )
				lines.add( line ) ;
			line = linp.readLine() ;
		}
		linp.close();
	}

	public static void main ( String args[] ) {
		try {
			new Compress ( new File ( "C:\\BNAServiceHub\\service.bat") ) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
