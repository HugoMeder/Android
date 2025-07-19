package de.bitsnarts.BNAHub.Camera.connectivity;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Inet4AddressWithNetworkPrefix {

	public final int networkPrefixLength;
	public final Inet4Address addr;
	private static final int byteMasks[] = {
		0b00000000,
		0b10000000,
		0b11000000,
		0b11100000,
		0b11110000,
		0b11111000,
		0b11111100,
		0b11111110
		} ;
	
	public Inet4AddressWithNetworkPrefix ( int networkPrefixLength, Inet4Address addr ) {
		this.networkPrefixLength = networkPrefixLength ;
		this.addr = addr ;
	}

	public Inet4Address getBroadcstAddress () {
		byte[] rvb = new byte [4] ;
		byte[] ab = addr.getAddress() ;
		int nb = networkPrefixLength ;
		int bindex = 0 ; 
		while ( nb >= 8 ) {
			rvb[bindex] = ab[bindex] ;
			bindex++ ;
			nb -= 8 ;
		}
		if ( nb != 0 ) {
			int mask = byteMasks[nb] ;
			int _b = ab[bindex]&mask ;
			mask = ~mask  ;
			_b = _b | mask ;
			rvb[bindex] = (byte) _b ;
			bindex++ ;
		}
		for ( int i = bindex ; i < 4 ; i++ ) {
			rvb[i] = (byte) 0xff ;
		}
		try {
			return (Inet4Address) Inet4Address.getByAddress(rvb) ;
		} catch (UnknownHostException e) {
			throw new Error ( e ) ;
		}
	}
	public boolean shareNetwork(Inet4AddressWithNetworkPrefix la) {
		if ( networkPrefixLength != la.networkPrefixLength )
			return false ;
		byte[] b = addr.getAddress() ;
		byte[] b2 = la.addr.getAddress() ;
		int byteIndex = 0 ;
		int l = networkPrefixLength ;
		while ( l >= 8 ) {
			if ( b[byteIndex] != b2[byteIndex] )
				return false ;
			byteIndex++ ;
			l -= 8 ;
		}
		if ( l != 0 ) {
			int mask = byteMasks[l] ;
			if ( (b[byteIndex]&mask) != (b2[byteIndex]&mask) ) {
				return false ;
			}
		}
		return true;
	}
}
