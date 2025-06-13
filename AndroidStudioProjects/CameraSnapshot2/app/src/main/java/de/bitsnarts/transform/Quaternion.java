package de.bitsnarts.transform;
// SHA1 File Hash, BEGIN
// hash=loiFqH6HQdn40IBpwh9ey-MCoaCA
//     date=Sat Jan 19 17:51:43 CET 2013
//     svn revision=23228
// hash=tc6Izgot-I1SyqVKwt5xCY_Yi-GA
//     date=Tue Jan 22 17:09:24 CET 2013
//     svn log comment=physics prototype: java2cpp, some admin done
//     svn revision=23315
// hash=MzlqQT27V9C85OcYcEmaez12IZNA
//     date=Wed Jan 23 14:51:35 CET 2013
//     svn log comment=physics prototype: java2cpp, some admin done(3)
//     svn revision=23335
// hash=7rQi0bligLFbpjv1TsrWS-o31RKA
//     date=Tue Oct 22 13:20:47 CEST 2013
//     svn log comment=(TAG) icido.Math in Solver PrototypeSystems
//     svn revision=27678
// SHA1 File Hash, END

import java.io.Serializable;
import java.util.Random;

/**
 * 
 * A class to represent Rotations
 * @author hum at IC_IDO
 *
 */
public class Quaternion  implements Serializable {

	private static final long serialVersionUID = 3617292333167884598L;
	double i, j, k, r ;
	double mat[][] ;
	boolean matValid ;
	float tmp3[] ;
	static Quaternion R = new Quaternion ( 1, 0, 0, 0 ) ;
	static Quaternion I = new Quaternion ( 0, 1, 0, 0 ) ;
	static Quaternion J = new Quaternion ( 0, 0, 1, 0 ) ;
	static Quaternion K = new Quaternion ( 0, 0, 0, 1 ) ;
	static final double radPerDegree = Math.PI/180.0 ;

	public Quaternion () {
		r = 1 ;
		}

	public Quaternion ( double r, double i, double j, double k ) {
		this.i = i ;
		this.j = j ;
		this.k = k ;
		this.r = r ;
		}
    
    public Quaternion ( double[] quat ) {
        this.r = quat[0] ;
        this.i = quat[1] ;
        this.j = quat[2] ;
        this.k = quat[3] ;
        }

	public Quaternion times ( Quaternion q ) {
		double _r = r*q.r - i*q.i - j*q.j - k*q.k ;

		double _i = r*q.i + i*q.r + j*q.k - k*q.j ;
		double _j = r*q.j + j*q.r + k*q.i - i*q.k ;
		double _k = r*q.k + k*q.r + i*q.j - j*q.i ;
		return new Quaternion ( _r, _i, _j, _k ) ;
		}

	public Quaternion times ( double f ) {
		return new Quaternion ( r*f, i*f, j*f, k*f ) ;
		}


	public Quaternion normalize () {
		return times ( 1.0/norm() ) ;
		}

	public double getR() { return r ; }
	public double getI() { return i ; }
	public double getJ() { return j ; }
	public double getK() { return k ; }

	public void times ( Quaternion res, Quaternion q ) {
		double _r = r*q.r - i*q.i - j*q.j - k*q.k ;

		double _i = r*q.i + i*q.r + j*q.k - k*q.j ;
		double _j = r*q.j + j*q.r + k*q.i - i*q.k ;
		double _k = r*q.k + k*q.r + i*q.j - j*q.i ;
		res.r = _r ;
		res.i = _i ;
		res.j = _j ;
		res.k = _k ;
		res.matValid = false ;
		}

	public void add ( Quaternion res, Quaternion q ) {
		res.r = r+q.r ;
		res.i = i+q.i ;
		res.j = j+q.j ;
		res.k = k+q.k ;
		res.matValid = false ;
		}

	public void sub ( Quaternion res, Quaternion q ) {
		res.r = r-q.r ;
		res.i = i-q.i ;
		res.j = j-q.j ;
		res.k = k-q.k ;
		res.matValid = false ;
		}

	public Quaternion add ( Quaternion q ) {
		return new Quaternion ( r+q.r, i+q.i, j+q.j, k+q.k ) ;
		}


	public Quaternion sub ( Quaternion q ) {
		return new Quaternion ( r-q.r, i-q.i, j-q.j, k-q.k ) ;
		}


	public static Quaternion fromAxis ( double phi, double nx, double ny, double nz ) {
		phi/= 2.0 ;
		double c = Math.cos ( phi ) ;
		double s = Math.sin ( phi ) ;

		return new Quaternion ( c, s*nx, s*ny, s*nz ) ;
		}

	public static Quaternion fromMatrix ( double R[][] ) {
		return QuaternionTools.fromMatrix(R) ;
	}
	
	public double norm () {
		return Math.sqrt ( r*r+i*i+j*j+k*k ) ;
		}

	public double normSqr () {
		return r*r+i*i+j*j+k*k ;
		}


	private Quaternion conjugate () {
		return new Quaternion ( r, -i, -j, -k ) ;
		}


	public static Quaternion fromEulerDegrees ( float deg[] ) {
		return fromEuler ( radPerDegree*(double)deg[0], radPerDegree*(double)deg[1], radPerDegree*(double)deg[2] ) ;
		}

	private static Quaternion fromEuler2 ( double heading, double pitch, double roll ) {
		Quaternion h = fromAxis ( heading, 0, 0, 1 ) ;
		Quaternion p = fromAxis ( pitch, 1, 0, 0 ) ;
		Quaternion r = fromAxis ( roll, 0, 1, 0 ) ;
		return ( h.times ( p.times ( r ) ) ) ;
//		return ( r.times ( p.times ( h ) ) ) ;
		}

	public static Quaternion fromEulerDegrees ( double heading, double pitch, double roll ) {
		Quaternion h = fromAxis ( radPerDegree*heading, 0, 1, 0 ) ;
		Quaternion p = fromAxis ( radPerDegree*pitch, 1, 0, 0 ) ;
		Quaternion r = fromAxis ( radPerDegree*roll, 0, 0, -1 ) ;
		return ( h.times ( p.times ( r ) ) ) ;
//		return ( r.times ( p.times ( h ) ) ) ;
		}

	public static Quaternion fromEuler ( double haeding, double pitch, double roll ) {
		haeding /= 2.0 ;
		pitch /= 2.0 ;
		roll /= 2.0 ;
		double c_h = Math.cos ( haeding ) ;
		double s_h = Math.sin ( haeding ) ;
		double c_p = Math.cos ( pitch ) ;
		double s_p = Math.sin ( pitch ) ;
		double c_r = Math.cos ( roll ) ;
		double s_r = Math.sin ( roll ) ;
/*		return new Quaternion ( c_r*c_p*c_h+s_r*s_p*s_h,
					c_r*c_h*s_p+s_r*c_p*s_h,
					-c_r*s_p*s_h+s_r*c_p*c_h,
					c_r*c_p*s_h-s_r*c_h*s_p ) ;
*/
		return new Quaternion ( c_r*c_p*c_h - s_r*s_p*s_h,
					c_h*c_r*s_p - s_h*c_p*s_r,
					c_h*c_p*s_r + s_h*c_r*s_p,
					c_h*s_p*s_r + s_h*c_p*c_r ) ;
		}

/*
	public void getMatrix33 ( double mat[][] ) {

		mat[0][0] = r*r+i*i-j*j-k*k ;
		mat[1][0] = 2.0*(r*k+i*j) ;
		mat[2][0] = 2.0*(-r*j+k*i) ;

		mat[0][1] = 2.0*(-r*k+i*j) ;
		mat[1][1] = r*r+j*j-i*i-k*k ;
		mat[2][1] = 2.0*(r*i+j*k) ;

		mat[0][2] = 2.0*(r*j+k*i) ;
		mat[1][2] = 2.0*(-r*i+j*k) ;
		mat[2][2] = r*r+k*k-i*i-j*j ;

		}

*/
	public void getEuler ( double hpr[] ) {

		double y_x = 2.0*(-r*k+i*j) ;
		double y_y = r*r+j*j-i*i-k*k ;
		double y_z = 2.0*(r*i+j*k) ;
		double xy = Math.sqrt ( y_x*y_x+y_y*y_y ) ;
		double p = Math.atan2 ( y_z, xy ) ;
		double h ;
		hpr[1] = p ;
		if ( xy == 0 )
			h = 0 ;
		else {
			h = Math.atan2 ( -y_x, y_y ) ;
			}
		Quaternion q = fromEuler ( h, p, 0 ).conjugate() ;
		Quaternion q2 = q.times ( this ) ;
		double r = 2.0*Math.atan2 ( q2.j, q2.r ) ;
		hpr[0] = h ;
		hpr[1] = p ;
		hpr[2] = r ;
		}

	public String toString () {
		return "["+r+"; "+i+", "+j+", "+k+"]" ;
		}

	public void getEulerDegrees ( float hpr[] ) {
		double tmp[] = new double [3] ;
		getEuler ( tmp ) ;
		hpr[0] = (float) (tmp[0]/radPerDegree) ;
		hpr[1] = (float) (tmp[1]/radPerDegree) ;
		hpr[2] = (float) (tmp[2]/radPerDegree) ;
		}

	public void getMatrix33 ( double mat[][] ) {

		mat[0][0] = r*r+i*i-j*j-k*k ;
		mat[1][0] = 2.0*(r*k+i*j) ;
		mat[2][0] = 2.0*(-r*j+k*i) ;

		mat[0][1] = 2.0*(-r*k+i*j) ;
		mat[1][1] = r*r+j*j-i*i-k*k ;
		mat[2][1] = 2.0*(r*i+j*k) ;

		mat[0][2] = 2.0*(r*j+k*i) ;
		mat[1][2] = 2.0*(-r*i+j*k) ;
		mat[2][2] = r*r+k*k-i*i-j*j ;

		}


	public Vector3D apply ( Vector3D x ) {
		if ( mat == null ) {
			mat = new double [3][] ;
			mat[0] = new double[3] ;
			mat[1] = new double[3] ;
			mat[2] = new double[3] ;
			}
		if ( !matValid ) {
			getMatrix33 ( mat ) ;
			matValid = true ;
			}
		double x_ = x.x ;
		double y_ = x.y ;
		double z_ = x.z ;

		return new Vector3D (
			mat[0][0]*x_+mat[0][1]*y_+mat[0][2]*z_,
			mat[1][0]*x_+mat[1][1]*y_+mat[1][2]*z_,
			mat[2][0]*x_+mat[2][1]*y_+mat[2][2]*z_ ) ;
		}


/*	public void apply ( Vector3DVar res, Vector3D x ) {
		if ( mat == null ) {
			mat = new double [3][] ;
			mat[0] = new double[3] ;
			mat[1] = new double[3] ;
			mat[2] = new double[3] ;
			}
		if ( !matValid ) {
			getMatrix33 ( mat ) ;
			matValid = true ;
			}
		double x_ = x.x ;
		double y_ = x.y ;
		double z_ = x.z ;

		res.x = mat[0][0]*x_+mat[0][1]*y_+mat[0][2]*z_ ;
		res.y = mat[1][0]*x_+mat[1][1]*y_+mat[1][2]*z_ ;
		res.z = mat[2][0]*x_+mat[2][1]*y_+mat[2][2]*z_ ;
		}
*/
	
	void getMatrix332 ( double mat[][] ) {

		Quaternion q = normalize () ;
		Quaternion q_ = q.conjugate () ;
		Quaternion m[] = new Quaternion[3] ;

		m[0] = q.times ( I.times ( q_ ) ) ;
		m[1] = q.times ( J.times ( q_ ) ) ;
		m[2] = q.times ( K.times ( q_ ) ) ;

		for ( int i = 0 ; i < 3 ; i++ ) {
			mat[0][i] = m[i].i ;
			mat[1][i] = m[i].j ;
			mat[2][i] = m[i].k ;
			}
		}

	public Quaternion transpose () {
		return new Quaternion ( r, -i, -j, -k ) ;
	}
	
	public boolean equals ( Quaternion q ) {
		return q.i==i && q.j == j && q.k == k && q.r == r ;
	}
	
	public boolean equals ( Object q ) {
		try {
			return equals ( (Quaternion)q ) ;
		} catch ( Throwable th ) {
			return false ;
		}
	}
	
	public double[] asDoubleArray() {
		double[] rv = new double[4];
		rv[0] = r ;
		rv[1] = i ;
		rv[2] = j ;
		rv[3] = k ;
		return rv;
	}
	
	public Quaternion sqrt () {
		double c = r ;
		double s = Math.sqrt( i*i+j*j+k*k ) ;
		if ( s == 0.0 ) {
			if ( c >= 0.0 ) {
				return new Quaternion ( Math.sqrt ( c ), 0.0, 0.0, 0.0 );  
			} else {
				return new Quaternion ( 0.0, Math.sqrt ( c ), 0.0, 0.0 ) ;
			}
		}
		double l = Math.sqrt ( c*c+s*s ) ;
		double l_ = (c+l) ;
		if ( l_ <= 0.0 )
			l_ = 0.0 ;
		double gamma = Math.sqrt( 0.5*(c+l) ) ;
		double sigma = s/(2.0*gamma) ;
		return new Quaternion ( gamma, sigma*i/s, sigma*j/s, sigma*k/s ) ;
	}

	public Quaternion inverse() {
		double len2 = normSqr () ;
		return new Quaternion ( r/len2,-i/len2,-j/len2,-k/len2 ) ;
	}
}
