package inducesmile.com.camera2;

import java.util.TreeMap;

public enum ServiceToClientCmds {
	PRINTLN(1), PREVIEW(2), IMAGE(3) ;
	
	static TreeMap<Integer,ServiceToClientCmds> cmdsByCode ;
	
	private int code;

	ServiceToClientCmds(int code ) {
		this.code = code ;
	}
	
	public int getCode () {
		return code ;
	}
	
	public static ServiceToClientCmds getByCode (int code ) {
		return getMap ().get( code ) ;
	}

	private static TreeMap<Integer,ServiceToClientCmds> getMap() {
		if ( cmdsByCode == null ) {
			TreeMap<Integer, ServiceToClientCmds> rv = new TreeMap<Integer,ServiceToClientCmds> () ;
			for ( ServiceToClientCmds c : values () ) {
				rv.put( c.code, c ) ;
			}
			cmdsByCode = rv ;
		}
		return cmdsByCode ;
	}
}
