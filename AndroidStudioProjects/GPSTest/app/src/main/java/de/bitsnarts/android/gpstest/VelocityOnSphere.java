package de.bitsnarts.android.gpstest;

import java.util.Date;

public class VelocityOnSphere {

    private boolean hasLastPosition ;
    private double lastTimeSecs ;
    private double lastLon, lastLat ;
    private double r ;
    public double velNord, velEast ;
    public boolean hasVel;

    VelocityOnSphere ( double r ) {
        this.r = r ;
    }

    VelocityOnSphere () {
        this ( (6378137.0+6356752.314245)/2 ) ;
    }

    public void setPosition(Date d, double longitudeDeg, double latitudeDeg) {
        double timeSecs = ((double)d.getTime())/1000.0 ;
        double longitude = longitudeDeg*Math.PI/180.0 ;
        double latitude = latitudeDeg*Math.PI/180.0 ;
        if ( hasLastPosition && lastTimeSecs < timeSecs ) {
            double deltaLon = longitude-lastLon ;
            double deltaLat = latitude-lastLat ;
            double deltaT = timeSecs - lastTimeSecs ;
            double lonVel = deltaLon/deltaT ;
            double latVel = deltaLat/deltaT ;
            velNord = latVel*r ;
            velEast = lonVel*r*Math.cos( (latitude+lastLat)/2.0 ) ;
            hasVel = true ;
        }
        hasLastPosition = true ;
        lastLon = longitude ;
        lastLat = latitude ;
        lastTimeSecs = timeSecs ;
    }
}
