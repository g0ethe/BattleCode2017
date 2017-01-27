package woodleague.bot.archon;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import woodleague.bot.Bot;
import woodleague.util.Broadcast;
import woodleague.util.GameState;

// TODO:
// - be more intelligent about when to hire
// - move away from own flowers before hiring as to not have
//	 two flowers intersect
// - donate bullets once you have more than like 2000 or so
public class Archon extends Bot {

	public static final int TREES_PER_GARDENER = 7;

	public static void run() throws GameActionException {
		while(true) {
			long turn = rc.getRoundNum();

			Broadcast.updateUnitCount();
			if(shouldHireGardener()) hireGardener();
			moveAwayForGardenerTesting();

			if(turn == rc.getRoundNum()) Clock.yield();
		}
	}

	// for gardener debugging purposes TODO: delete
	private static void moveAwayForGardenerTesting() throws GameActionException {
		RobotInfo[] asd = rc.senseNearbyRobots();
		if(asd.length > 0) {
			Direction dir = rc.getLocation().directionTo(GameState.enemyCenter);
			if(!rc.hasMoved()) tryMove(dir, 120, 5);
		}
	}

	// TODO: don't hire gardener near another gardeners flower
	private static void hireGardener() throws GameActionException {
		if(!rc.isBuildReady() || !rc.hasRobotBuildRequirements(RobotType.GARDENER)) return;
		Direction dir = rc.getLocation().directionTo(GameState.enemyCenter);
		for(int i = 0; i <= 180; i += 5) {
			if(rc.canHireGardener(dir.rotateLeftDegrees(i))) { rc.hireGardener(dir.rotateLeftDegrees(i)); break; }
			if(rc.canHireGardener(dir.rotateRightDegrees(i))) { rc.hireGardener(dir.rotateRightDegrees(i)); break; }
		}
	}

	// TODO: make more intelligent choices
	private static boolean shouldHireGardener() throws GameActionException {

		// don't waste bullets if we can win this turn
		if(GameState.canWinByPoints()) rc.donate(rc.getTeamBullets());

		// at the start of a match, hire one gardener
		if(rc.getRobotCount() <= GameState.startArchons) return true;

		return false;
	}


}
