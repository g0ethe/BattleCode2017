package woodleague.util;

import battlecode.common.BodyInfo;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Geometry {

	public static RobotController rc;

	public static MapLocation centerOf(MapLocation... locations) {
		if(locations.length == 0) return null;
		if(locations.length == 1) return locations[0];
		float x = 0, y = 0;
		for(MapLocation location : locations) {
			x += location.x; y += location.y;
		}
		return new MapLocation(x/locations.length, y/locations.length);
	}

    /*
     * returns if given body is in specified rectangle
     *
     * @ param:
     * 		BodyInfo body: the body to be checked against the rectangle
     * 		Direction dir: the direction the rectangle is aligned with
     * 		MapLocation M: middle point of outer-most edge of rectangle
     * 		float h: half the rectangles height
     */
    public static boolean isInRectangle(BodyInfo body, Direction dir, MapLocation M, float h) {
    	 float alpha = Math.abs(M.directionTo(body.getLocation()).radiansBetween(dir));
    	 if(alpha >= Math.PI/2) return false;
    	 float d = M.distanceTo(body.getLocation());
    	 float x = (float)(d * Math.sin(alpha));
    	 return x - h <= body.getRadius();
    }

}
