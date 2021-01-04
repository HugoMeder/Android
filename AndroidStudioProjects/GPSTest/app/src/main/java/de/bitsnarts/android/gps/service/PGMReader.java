package de.bitsnarts.android.gps.service;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class PGMReader implements FunctionOnSphere {

	private File file;
	private Map<String,String> headerFields = new TreeMap<String,String> () ;
	private int width;
	private int height;
	private int maxVal;
	private double[][] data;
	private int start;
	private double degreesPerPixel;
	private double scale;
	private double offset;
	
	public PGMReader ( File file ) throws IOException {
		this.file = file ;
		readHeader () ;
		long len = file.length() ;
		start = (int) (len - 2*width*height) ;
		degreesPerPixel = 360.0/width ;
		scale = Double.parseDouble( headerFields.get( "Scale") ) ;
		offset = Double.parseDouble( headerFields.get( "Offset") ) ;
	}

	private void readData() throws IOException {
		data = new double[width][height] ;
		FileInputStream in = new FileInputStream ( file ) ;
		for ( int i = 0 ; i < start ; i++ )
			in.read() ;
		for ( int y = 0 ; y < height ; y++ ) {
			for ( int x = 0 ; x < width ; x++ ) {
				int b1 = in.read( ) ;
				int b2 = in.read( ) ;
				int val = b1*256+b2 ;
				data[x][y] = (val+offset)*scale ;
			}
		}
		in.close();
	}

	private void readHeader() throws IOException {
		FileReader rdr = new FileReader ( file ) ;
		LineNumberReader linp = new LineNumberReader ( rdr ) ;
		String line = linp.readLine() ;
		if ( !line.equals( "P5") ) {
			linp.close();
			throw new IOException ( "!line.equals( \"p5\")" ) ; 
		}
		line = linp.readLine() ;
		while ( line.charAt( 0 ) == '#' ) {
			int pos = line.indexOf( ' ', 2 ) ;
			String key = line.substring( 2, pos ) ;
			String val = line.substring( pos+1 ) ;
			headerFields.put( key, val ) ;
			line = linp.readLine() ;			
		}
		StringTokenizer tok = new StringTokenizer ( line ) ;
		width = Integer.parseInt( tok.nextToken() ) ;
		height = Integer.parseInt( tok.nextToken() ) ;
		line = linp.readLine() ;			
		maxVal = Integer.parseInt( line ) ;
		linp.close();
	}
	
	public static void main ( String[] args ) {
		File file = new File ( "data/egm84-30/geoids/egm84-30.pgm" ) ;
		try {
			PGMReader rdr = new PGMReader ( file ) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double[][] getData() {
		if ( data == null )
			try {
				readData () ;
			} catch (IOException e) {
				throw new Error ( e ) ;
			}
		return data ;
	}
	
	public double getGeoidHeightAt ( double longitudeDegrees, double latitudeDegrees ) throws IOException {
		while ( longitudeDegrees < 0 )
			longitudeDegrees += 360.0 ;
		double degreesFromNorthpole = 90.0 - latitudeDegrees ;
		int pix_x = (int)Math.floor( longitudeDegrees/degreesPerPixel ) ;
		int pix_y = (int)Math.floor( degreesFromNorthpole/degreesPerPixel ) ;
		FileInputStream in = new FileInputStream ( file ) ;
		long pos0 = start + 2*(pix_y*width+pix_x ) ;
		long pos1 = start + 2*((pix_y+1)*width+pix_x ) ;
		in.skip( pos0 ) ;
		double h00 = readValue ( in ) ;
		double h10 = readValue ( in ) ;
		in.skip( pos1-(pos0+4 ) ) ;
		double h01 = readValue ( in ) ;
		double h11 = readValue ( in ) ;
		in.close();
		double deltaX = longitudeDegrees-pix_x*degreesPerPixel ;
		double deltaY = degreesFromNorthpole-pix_y*degreesPerPixel ;
		double wx1 = deltaX/degreesPerPixel ;
		double wx0 = 1.0 -wx1 ;
		double wy1 = deltaY/degreesPerPixel ;
		double wy0 = 1.0 - wy1  ;
		double rv = wx0*(wy0*h00+wy1*h01)+wx1*(wy0*h10+wy1*h11) ;
		return rv ;
	}

	private double readValue(FileInputStream in) throws IOException {
		int b1 = in.read( ) ;
		int b2 = in.read( ) ;
		int val = b1*256+b2 ;
		return val*scale+offset ;
	}
	
	public void writeAsPGMBin ( File file ) throws IOException {
		FileOutputStream out = new FileOutputStream ( file ) ;
		DataOutputStream dout = new DataOutputStream ( out ) ;
		dout.writeLong( 4572934265437L ) ;
		dout.writeInt( 1 );
		dout.writeInt( width );
		dout.writeInt( height );
		dout.writeDouble( offset );
		dout.writeDouble( scale );
		int n = width*height ;
		FileInputStream in = new FileInputStream ( this.file ) ;
		in.skip( start ) ;
		for ( int i = 0 ; i < n ; i++ ) {
			int b1 = in.read() ;
			int b2 = in.read() ;
			dout.write( b1 );
			dout.write( b2 );
		}
		in.close();
		out.close();
	}

	@Override
	public double evaluate(double longitudeDegrees, double latitudeDegrees) {
		if ( data == null )
			try {
				readData () ;
			} catch (IOException e) {
				throw new Error ( e ) ;
			}
		while ( longitudeDegrees < 0 )
			longitudeDegrees += 360.0 ;
		double degreesFromNorthpole = 90.0 - latitudeDegrees ;
		int pix_x = (int)Math.floor( longitudeDegrees/degreesPerPixel ) ;
		int pix_y = (int)Math.floor( degreesFromNorthpole/degreesPerPixel ) ;
		double h00 = data[pix_x][pix_y] ;
		double h10 = data[(pix_x+1)%width][pix_y] ;
		double h01 = data[pix_x][pix_y+1] ;
		double h11 = data[(pix_x+1)%width][pix_y+1] ;
		double deltaX = longitudeDegrees-pix_x*degreesPerPixel ;
		double deltaY = degreesFromNorthpole-pix_y*degreesPerPixel ;
		double wx1 = deltaX/degreesPerPixel ;
		double wx0 = 1.0 -wx1 ;
		double wy1 = deltaY/degreesPerPixel ;
		double wy0 = 1.0 - wy1  ;
		double rv = wx0*(wy0*h00+wy1*h01)+wx1*(wy0*h10+wy1*h11) ;
		return rv ;
	}
}
