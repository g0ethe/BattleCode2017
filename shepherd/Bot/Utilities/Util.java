package shepherd.Bot.Utilities;


import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Util {

	/*
	 * returns the robot with the lowest amount of health of the given type from given array
	 */
	public static RobotInfo getLowestOfTypeFromList(RobotType type, RobotInfo[] robots) {
		RobotInfo lowest = null;
		double minHP = Double.MAX_VALUE;
		for(RobotInfo robot : robots) {
			if(robot.getType() == type && robot.getHealth() < minHP) {
				lowest = robot;
				minHP = robot.getHealth();
			}
		}
		return lowest;
	}

}
