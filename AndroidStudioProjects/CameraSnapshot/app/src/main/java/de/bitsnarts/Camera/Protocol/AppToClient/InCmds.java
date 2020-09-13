package de.bitsnarts.Camera.Protocol.AppToClient;

import java.util.TreeMap;

public enum InCmds {
	PRINTLN(1) ;
	
	static TreeMap<Integer,InCmds> cmdsByCode ;
	
	private int code;

	InCmds ( int code ) {
		this.code = code ;
	}
	
	public int getCode () {
		return code ;
	}
	
	public static InCmds getByCode ( int code ) {
		return getMap ().get( code ) ;
	}

	private static TreeMap<Integer,InCmds> getMap() {
		if ( cmdsByCode == null ) {
			TreeMap<Integer, InCmds> rv = new TreeMap<Integer,InCmds> () ;
			for ( InCmds c : values () ) {
				rv.put( c.code, c ) ;
			}
			cmdsByCode = rv ;
		}
		return cmdsByCode ;
	}
}
