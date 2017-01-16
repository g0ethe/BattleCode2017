package shepherd.Bot.Utilities;

import java.util.List;

import battlecode.common.BodyInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.TreeInfo;

public class Geometry {

	/* given a line, by two points L1 = (L1x, L1y) and L2 = (L2x, L2y) and a circle by center C = (Cx, Cy) and radius r,
	 * calculates if the line intersects with the circle */
	public static boolean lineIntersectsCircle(float L1x, float L1y, float L2x, float L2y, float Cx, float Cy, float r) {
		if(L1x != L2x && L1y != L2y) {
			float m = (L2y - L1y) / (L2x - L1x);
			float b = -1*m * L2x + L2y;
			float x = (m*m + 1);
			float y = 2*(m*b - m*Cy - Cx);
			float z = Cy*Cy - r*r + Cx*Cx - 2*b*Cy + b*b;
			return y*y - 4*x*z >= 0;
		}
		else if(L1x == L2x && L1y != L2y) {
			float dx = (Cx - L1x < 0) ? (L1x - Cx) : (Cx - L1x);
			return dx < r;
		}
		else if(L1x != L2x && L1y == L2y) {
			float dy = (Cy - L1y < 0) ? (L1y - Cy) : (Cy - L1y);
			return dy < r;
		}
		else return (L1x - Cx)*(L1x - Cx) + (L1y - Cy)*(L1y - Cy) <= r*r;
	}


	/*
	 * returns the distance between the borders of two given circles,
	 * defined by their radii and centers,
	 * or by body info
	 */
	public static float distanceBetween(MapLocation centerA, float radiusA, MapLocation centerB, float radiusB) {
		if(centerA == null || centerB == null) return -1;
		return centerA.distanceTo(centerB) - radiusA - radiusB;
	}
	public static float distanceBetween(MapLocation center, float radius, BodyInfo body) {
		if(body == null) return -1;
		return distanceBetween(center, radius, body.getLocation(), body.getRadius());
	}
	public static float distanceBetween(BodyInfo body, MapLocation center, float radius) {
		return distanceBetween(center, radius, body);
	}
	public static float distanceBetween(BodyInfo bodyA, BodyInfo bodyB) {
		if(bodyA == null) return -1;
		return distanceBetween(bodyA.getLocation(), bodyA.getRadius(), bodyB);
	}


	/*
	 * returns the nearest thing from given list or array to the relative location
	 */
	public static MapLocation getNearest(List<MapLocation> locations, MapLocation relativeLocation) throws GameActionException {
		float minDist = Float.MAX_VALUE;
		MapLocation nearest = null;
		for(int index = 0; index < locations.size(); index++) {
			MapLocation loc = locations.get(index);
			float dist = relativeLocation.distanceTo(loc);
			if(dist < minDist) {
				nearest = loc;
				minDist = dist;
			}
		}
		return nearest;
	}
	public static TreeInfo getNearest(TreeInfo[] trees, MapLocation relativeLocation) throws GameActionException {
		float minDist = Float.MAX_VALUE;
		TreeInfo nearest = null;
		for(TreeInfo tree : trees) {
			float dist = relativeLocation.distanceTo(tree.location);
			if(dist < minDist) {
				nearest = tree;
				minDist = dist;
			}
		}
		return nearest;
	}
	public static MapLocation getNearest(MapLocation[] locations, MapLocation relativeLocation) throws GameActionException {
		float minDist = Float.MAX_VALUE;
		MapLocation nearest = null;
		for(MapLocation loc : locations) {
			float dist = relativeLocation.distanceTo(loc);
			if(dist < minDist) {
				nearest = loc;
				minDist = dist;
			}
		}
		return nearest;
	}



}
