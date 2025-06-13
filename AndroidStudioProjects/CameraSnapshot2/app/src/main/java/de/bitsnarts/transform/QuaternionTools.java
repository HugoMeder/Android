package de.bitsnarts.transform;
// SHA1 File Hash, BEGIN
// hash=mY2pJvvkaoaOrkhSYzD6VyOlDzEA
//     date=Sat Jan 19 17:51:43 CET 2013
//     svn revision=23228
// hash=AzHSubu5aSkntmAKuPsSPqXORrEA
//     date=Tue Jan 22 17:09:24 CET 2013
//     svn log comment=physics prototype: java2cpp, some admin done
//     svn revision=23315
// hash=EXsuTQoXmH2CeCqG-Sq39gkR9JGA
//     date=Tue Feb 19 18:40:22 CET 2013
//     svn log comment=Quaternion: from Matrix
//     svn revision=23715
// hash=adsCv50Ng9fg_n3nmn6o655_xPOA
//     date=Tue Oct 22 13:20:47 CEST 2013
//     svn log comment=(TAG) icido.Math in Solver PrototypeSystems
//     svn revision=27678
// SHA1 File Hash, END

import java.util.Random;

public class QuaternionTools {

	public static void main ( String args [] ) {
		Random rand = new Random ( 2434554 );
		QuaternionTools t = new QuaternionTools () ;
		for ( int i = 0 ; i < 10000 ; i++ ) {
			Quaternion q = new Quaternion ( rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian() ).normalize();
			t.test ( q ) ;			
			System.out.println ( "test "+i ) ;
		}
		t.test( new Quaternion ( 1, 0, 0, 0 ) ) ;
	}

	private void test(Quaternion q) {
		double[][] R = new double[3][3];
		q.getMatrix33( R ) ;
		q = fromMatrix ( R ) ;

		double[][] R2 = new double[3][3];
		q.getMatrix33( R2 ); 
		double err = 0 ;
		for ( int i = 0 ; i < 3 ; i++ )
			for ( int j = 0 ; j < 3 ; j++ ) {
				double val = R[i][j]-R2[i][j]; 
				err += val*val ;
			}
		err = Math.sqrt ( err ) ;
		System.out.println ( "err "+err ) ;
		if ( err > 1e-14 )
			throw new Error ( "error "+err ) ;
		//double nl = dot ( tmp, axis[2] );
		//double tst = cos*cos+sin*sin;
		//System.out.println ( "tst "+tst+" nl "+nl ) ;
	}

	static Quaternion fromMatrix(double[][] R) {
		
// from http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
/*		
		double m00 = R[0][0] ;
		double m01 = R[0][1] ;
		double m02 = R[0][2] ;
		double m10 = R[1][0] ;
		double m11 = R[1][1] ;
		double m12 = R[1][2] ;
		double m20 = R[2][0] ;
		double m21 = R[2][1] ;
		double m22 = R[2][2] ;
		
		double tr = m00 + m11 + m22 ;
		double qw ;
		double qx ;
		double qy ;
		double qz ;
		if (tr > 0) { 
			  double S = Math.sqrt(tr+1.0) * 2; // S=4*qw 
			  qw = 0.25 * S;
			  qx = (m21 - m12) / S;
			  qy = (m02 - m20) / S; 
			  qz = (m10 - m01) / S; 
			} else if ((m00 > m11)&(m00 > m22)) { 
			  double S = Math.sqrt(1.0 + m00 - m11 - m22) * 2; // S=4*qx 
			  qw = (m21 - m12) / S;
			  qx = 0.25 * S;
			  qy = (m01 + m10) / S; 
			  qz = (m02 + m20) / S; 
			} else if (m11 > m22) { 
			  double S = Math.sqrt(1.0 + m11 - m00 - m22) * 2; // S=4*qy
			  qw = (m02 - m20) / S;
			  qx = (m01 + m10) / S; 
			  qy = 0.25 * S;
			  qz = (m12 + m21) / S; 
			} else { 
			  double S = Math.sqrt(1.0 + m22 - m00 - m11) * 2; // S=4*qz
			  qw = (m10 - m01) / S;
			  qx = (m02 + m20) / S;
			  qy = (m12 + m21) / S;
			  qz = 0.25 * S;
			}
		return new Quaternion ( qw, qx, qy, qz ) ;
*/
		
		double m00 = R[0][0] ;
		double m01 = R[0][1] ;
		double m02 = R[0][2] ;
		double m10 = R[1][0] ;
		double m11 = R[1][1] ;
		double m12 = R[1][2] ;
		double m20 = R[2][0] ;
		double m21 = R[2][1] ;
		double m22 = R[2][2] ;
		
		double scale = (m00*m00+m01*m01+m02*m02+m10*m10+m11*m11+m12*m12+m20*m20+m21*m21+m22*m22)/3.0 ; 
		scale = Math.sqrt ( scale ) ;
		double tr = m00 + m11 + m22 ;
		double qw ;
		double qx ;
		double qy ;
		double qz ;
		if (tr > 0) { 
			  double S = Math.sqrt(tr+scale) * 2; // S=4*qw 
			  qw = 0.25 * S;
			  qx = (m21 - m12) / S;
			  qy = (m02 - m20) / S; 
			  qz = (m10 - m01) / S; 
		} else if ((m00 > m11)&(m00 > m22)) { 
			  double S = Math.sqrt(scale + m00 - m11 - m22) * 2; // S=4*qx 
			  qw = (m21 - m12) / S;
			  qx = 0.25 * S;
			  qy = (m01 + m10) / S; 
			  qz = (m02 + m20) / S; 
		} else if (m11 > m22) { 
			  double S = Math.sqrt(scale + m11 - m00 - m22) * 2; // S=4*qy
			  qw = (m02 - m20) / S;
			  qx = (m01 + m10) / S; 
			  qy = 0.25 * S;
			  qz = (m12 + m21) / S; 
		} else { 
			  double S = Math.sqrt(scale + m22 - m00 - m11) * 2; // S=4*qz
			  qw = (m10 - m01) / S;
			  qx = (m02 + m20) / S;
			  qy = (m12 + m21) / S;
			  qz = 0.25 * S;
			}
		return new Quaternion ( qw, qx, qy, qz ) ;
		}

	static Quaternion fromMatrix2(double[][] R) {
		double[][] axis = getAxis ( R ) ;
		if ( axis == null )
			return new Quaternion ( 1, 0, 0, 0 ) ;
		double[] tmp = new double[3] ;
		xform ( R, axis[0], tmp ) ;
		/*
		double aa = dot ( axis[0], axis[0] );
		double bb = dot ( axis[1], axis[1] );
		double cc = dot ( axis[2], axis[2] );
		
		double ab = dot ( axis[0], axis[1] );
		double bc = dot ( axis[1], axis[2] );
		double ca = dot ( axis[2], axis[0] );
		*/
		
		double cos = dot ( tmp, axis[0] );
		double sin = dot ( tmp, axis[1] );
		double t = Math.sqrt ( (cos+1.0)/2.0 );
		if ( cos >= 0 ) {
			sin = sin*Math.sqrt( 0.5/((cos+1.0)) ) ;
			cos = t ;
		} else {
			if ( sin < 0 )
				sin = -Math.sqrt( 1.0 - t*t ) ;
			else
				sin = Math.sqrt( 1.0 - t*t ) ;
			cos = t ;
		}
		return new Quaternion ( cos, sin*axis[2][0], sin*axis[2][1], sin*axis[2][2] ) ;	
	}

	private static double dot(double[] a, double[] b) {
		double rv = 0;
		for ( int i = 0 ; i < 3 ; i++ )
			rv += a[i]*b[i] ;
		return rv;
	}

	private static void xform(double[][] R, double[] v, double rv[] ) {
		for ( int k = 0 ; k < 3 ; k++  ) {
			double sum = 0 ;
			for ( int i = 0 ; i < 3 ; i++ )
				sum += R[k][i]*v[i] ;
			rv[k] = sum ;
		}
	}

	static double[] cross ( double a[], double b[] ) {
		double[] c = new double[3];
		c[0] = a[1]*b[2]-a[2]*b[1] ;
		c[1] = a[2]*b[0]-a[0]*b[2] ;
		c[2] = a[0]*b[1]-a[1]*b[0] ;
		return c;
	}
	
	private static double[][] getAxis(double[][] R) {
		double max = 0 ;
		double min = 5 ;
		int minIndex = -1 ;
		for ( int i = 0 ; i < 3 ; i++ ) {
			double tot = 0 ;
			for ( int j = 0 ; j < 3 ; j++ ) {
				double val = R[j][i];
				if ( i == j )
					val -= 1 ;
				tot += val*val ;
			}
			if ( max < tot ) {
				max = tot ;
			}
			if ( min > tot ) {
				min = tot ;
				minIndex = i ;
			}
		}
		if ( max == 0.0 ) { // identity
			return null ;
		}
		double[][] rv = new double[3][3];
		double a[] = new double[3] ;
		double b[] = new double[3] ;
		int indexA ;
		int indexB ;
		switch ( minIndex ) {
		case 0:
			indexA = 1 ;
			indexB = 2 ;
			break ;
		case 1:
			indexA = 0 ;
			indexB = 2 ;
			break ;
		case 2:
			indexA = 0 ;
			indexB = 1 ;
			break ;
		default:
			throw new Error ("...") ;
		}
		for ( int i = 0 ; i < 3 ; i++ ) {
			a[i] = R[indexA][i] ;
			if ( i == indexA )
				a[i] -= 1.0 ;
			b[i] = R[indexB][i] ;
			if ( i == indexB )
				b[i] -= 1.0 ;
		}
		double[] c =cross ( a, b ) ;
		normalize ( c ) ;
		double[] c2 = cross ( c, a );
		normalize ( c2 ) ;
		double[] c3 = cross ( c, c2 );
		normalize ( c3 ) ;
		rv = new double[3][] ;
		rv[0] = c2 ;
		rv[1] = c3 ;
		rv[2] = c ;
		return rv ;
	}

	private static void normalize(double[] c) {
		double l = Math.sqrt( c[0]*c[0]+c[1]*c[1]+c[2]*c[2] ) ;
		c[0] /= l ;
		c[1] /= l ;
		c[2] /= l ;
	}
}
