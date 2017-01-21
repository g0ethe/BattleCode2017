package sheep.bot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import sheep.util.Broadcast;

public class Tank extends Bot {

	public static void run(RobotController rc) throws GameActionException {
		while(true) {
			Broadcast.updateUnitCount();
			Clock.yield();
		}
	}

}
