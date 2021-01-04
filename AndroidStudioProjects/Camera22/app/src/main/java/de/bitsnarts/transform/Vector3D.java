package de.bitsnarts.transform;
// SHA1 File Hash, BEGIN
// hash=ItjWqAOg_1BiTWbhJ5J5r6RpzYBA
//     date=Sat Jan 19 17:51:43 CET 2013
//     svn revision=23228
// hash=cIkTyUqactnoL5fLYj81AVWS0WHA
//     date=Tue Jan 22 17:09:24 CET 2013
//     svn log comment=physics prototype: java2cpp, some admin done
//     svn revision=23315
// hash=ysugMrDxYT0bW416K7mIeyJvyoLA
//     date=Tue Oct 22 13:20:47 CEST 2013
//     svn log comment=(TAG) icido.Math in Solver PrototypeSystems
//     svn revision=27678
// SHA1 File Hash, END

import java.io.Serializable;

public class Vector3D  implements Serializable {

	private static final long serialVersionUID = 3617860772106221617L;
	double x, y, z ;

	public Vector3D () {
		}

	public Vector3D ( double x, double y, double z ) {
		this.x = x ;
		this.y = y ;
		this.z = z ;
		}

	public Vector3D ( double val[] ) {
		this.x = val[0] ;
		this.y = val[1] ;
		this.z = val[2] ;
		}

	public Vector3D ( float val[] ) {
		this.x = val[0] ;
		this.y = val[1] ;
		this.z = val[2] ;
		}

	public Vector3D ( Vector3D v ) {
		this.x = v.x ;
		this.y = v.y ;
		this.z = v.z ;
		}


	public Vector3D sum ( Vector3D vector ) {
		return new Vector3D ( x+vector.x, y+vector.y, z+vector.z ) ;
		}


	public Vector3D diff ( Vector3D vector ) {
		return new Vector3D ( x-vector.x, y-vector.y, z-vector.z ) ;
		}

	public Vector3D cross ( Vector3D vector ) {
		return new Vector3D (
			y*vector.z-z*vector.y,
			z*vector.x-x*vector.z,
			x*vector.y-y*vector.x
			) ;
		}

	public double dot ( Vector3D vector ) {
		return x*vector.x+y*vector.y+z*vector.z ;
		}

/*	public void sum ( Vector3DVar res, Vector3D vector ) {
		res.x = x+vector.x ;
		res.y = x+vector.y ;
		res.z = x+vector.z ;
		}

	public void diff ( Vector3DVar res, Vector3D vector ) {
		res.x = x-vector.x ;
		res.y = x-vector.y ;
		res.z = x-vector.z ;
		}
*/
	public Vector3D times ( double f ) {
		return new Vector3D ( x*f, y*f, z*f ) ;
		}

	public String toString () {
		return x+";"+y+";"+z;
		}

	public double getX() {
		return x ;
		}

	public double getY() {
		return y ;
		}

	public double getZ() {
		return z ;
		}

    public double[] getArray()
    {
        double[] retVal = {x,y,z};
        return retVal;
    }
    /**
     * 
     * @return the absolute length of the vector
     */
	public double getLength () {
		return Math.sqrt ( x*x + y*y +z*z ) ;
		}
	public Vector3D normalize () {
		return times ( 1.0/getLength() ) ;
		}
		
	public boolean equals(Object obj) {
        if (!(obj instanceof Vector3D))
        {
            return false;
        }
        
        Vector3D vector = (Vector3D)obj;
        
		if ( (this.x == vector.x ) && (this.y == vector.y) && (this.z == vector.z) )
			return true;
		else
			return false;
		}

	public double norm() {
		return getLength();
	}
}
