package woodleague.util;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Util {

	public static RobotController rc;

	public static boolean isLineFreeFromOwnUnits(RobotInfo target) {
		Direction dir = rc.getLocation().directionTo(target.getLocation());
		float distance = rc.getLocation().distanceTo(target.getLocation());
		MapLocation center1 = rc.getLocation().add(dir, distance/3);
		MapLocation center2 = rc.getLocation().add(dir, distance*2/3);
		float radius = distance/6;
		RobotInfo[] allies = rc.senseNearbyRobots(center1, radius, rc.getTeam());
		if(allies.length > 0) return false;
		allies = rc.senseNearbyRobots(center2, radius, rc.getTeam());
		return allies.length <= 0;
	}

}
