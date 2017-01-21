package sheep.util;

import battlecode.common.BulletInfo;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Geometry {

	public enum Symmetry {
        NONE_DETERMINED,
        VERTICAL,
        HORIZONTAL,
        ROTATIONAL,
        VERTICAL_OR_ROTATIONAL,
        HORIZONTAL_OR_ROTATIONAL
    }
	public static RobotController rc;
	public static Symmetry symmetry;


	/*
	 * calculates and returns the current map's symmetry
	 */
	public static Symmetry getSymmetry() throws GameActionException {
		if(symmetry != null) return symmetry;

		MapLocation[] myLocs = rc.getInitialArchonLocations(rc.getTeam());
		MapLocation[] enemyLocs = rc.getInitialArchonLocations(rc.getTeam().opponent());

		if(myLocs.length < 1) return Symmetry.NONE_DETERMINED;

		if(checkAllX(myLocs, enemyLocs)) {
			if(myLocs.length == 1) return Symmetry.HORIZONTAL_OR_ROTATIONAL;
			else return  Symmetry.HORIZONTAL;
		}

		if(checkAllY(myLocs, enemyLocs)) {
			if(myLocs.length == 1) return Symmetry.VERTICAL_OR_ROTATIONAL;
			else return Symmetry.VERTICAL;
		}

		return Symmetry.ROTATIONAL;
	}

	/*
	 * returns the center of given points
	 */
	public static MapLocation centerOf(MapLocation... locations) {
		if(locations.length <= 0) return null;
		if(locations.length == 1) return locations[0];
		float x = 0, y = 0; int length = locations.length;
		for(int i = length; --i>=0;) {
			x += locations[i].x; y += locations[i].y;
		}
		return new MapLocation(x/length, y/length);
	}


    /*
     * returns distance from point P to line segment given by bullet
     */
    public static float distance(MapLocation P, BulletInfo bullet) {
    	float dx = bullet.getDir().getDeltaX(1), dy = bullet.getDir().getDeltaY(1);
    	float t = (P.x * dx + P.y * dy) / (dx*dx + dy*dy);
    	float Lx = t*dx - P.x, Ly = t*dy - P.y;
    	return (float)Math.sqrt(Math.min(Math.min(
    			P.distanceSquaredTo(bullet.getLocation().add(bullet.getDir(), bullet.getSpeed())),
    			P.distanceSquaredTo(bullet.getLocation())),
    			((Lx)*(Lx) + (Ly)*(Ly))));
    }

    /*
     * returns intersection of two lines, as represented by given bullets
     * returns null if A lies on B or if no intersection exists
     */
    public static MapLocation intersection(BulletInfo A, BulletInfo B) {
    	float x1 = A.getLocation().x, y1 = A.getLocation().y;
    	float x2 = B.getLocation().x, y2 = B.getLocation().y;
    	float dx1 = A.getDir().getDeltaX(1), dy1 = A.getDir().getDeltaY(1);
    	float dx2 = B.getDir().getDeltaX(1), dy2 = B.getDir().getDeltaY(1);

    	// check if same direction
    	float deg = A.getDir().degreesBetween(B.getDir());
    	if(deg == 0 || deg == 180 || deg == -180) {
    		return null;
    	}

    	float v = ((x2 - x1)*dy1 - y2*dx1 + y1*dx1) / (dy2*dx1 - dx2*dy1);
    	float x = x2 + v*dx2, y = y2 + v*dy2;
    	return new MapLocation(x,y);
    }

    /*
     * returns intersection of ray with circle
     * returns null if ray does not intersect given circle
     */
    public static MapLocation intersection(MapLocation o, Direction d, MapLocation c, float r) {
    	Direction l = new Direction(o,c);
    	float ldx = l.getDeltaX(1), ldy = l.getDeltaY(1);
    	float s = ldx*d.getDeltaX(1) + ldy * d.getDeltaY(1);
    	float l2 = ldx * ldx + ldy * ldy;
    	float r2 = r*r;
    	if(s < 0 && l2 > r2) return null;
    	float m2 = l2 - s*s;
    	if(m2 > r2) return null;
    	float q = (float)Math.sqrt(r*r - m2);
    	float t = (l2 > r2) ? s - q : s + q;
    	return o.add(d, t);
    }

	/*
	 * helper functions for calculating symmetry
	 */
	private static boolean checkAllX(MapLocation[] myLocs, MapLocation[] enemyLocs) throws GameActionException {
		for(int i = (myLocs.length - 1); i < myLocs.length; i++) {
	    	if (myLocs[i].x != enemyLocs[i].x) return false;
        }
	    return true;
	}
	private static boolean checkAllY(MapLocation[] myLocs, MapLocation[] enemyLocs) throws GameActionException {
		for (int i = (myLocs.length - 1); i < myLocs.length; i++) {
			if (myLocs[i].y != enemyLocs[i].y) return false;
		}
		return true;
	}
}
