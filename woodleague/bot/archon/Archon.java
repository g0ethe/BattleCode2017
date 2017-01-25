package woodleague.bot.archon;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import woodleague.bot.Bot;
import woodleague.util.Broadcast;
import woodleague.util.GameState;

public class Archon extends Bot {

	public static final int TREES_PER_GARDENER = 4;
	public static final int MAX_NUM_GARDENER_PER_ARCHON = 3;

	public static void run() throws GameActionException {
		while(true) {
			long turn = rc.getRoundNum();

			Broadcast.updateUnitCount();
			if(shouldHireGardener()) hireGardener();

			if(turn == rc.getRoundNum()) Clock.yield();
		}
	}

	private static void hireGardener() throws GameActionException {
		if(!rc.isBuildReady() || !rc.hasRobotBuildRequirements(RobotType.GARDENER)) return;
		Direction dir = rc.getLocation().directionTo(GameState.enemyCenter);
		for(int i = 0; i <= 180; i += 5) {
			if(rc.canHireGardener(dir.rotateLeftDegrees(i))) { rc.hireGardener(dir.rotateLeftDegrees(i)); break; }
			if(rc.canHireGardener(dir.rotateRightDegrees(i))) { rc.hireGardener(dir.rotateRightDegrees(i)); break; }
		}
	}

	private static boolean shouldHireGardener() throws GameActionException {

		// don't waste bullets if we can win this turn
		if(GameState.canWinByPoints()) rc.donate(rc.getTeamBullets());

		if(GameState.gardenerCount > MAX_NUM_GARDENER_PER_ARCHON * GameState.archonCount) return false;

		// at the start of a match, hire one gardener
		if(rc.getRobotCount() <= GameState.startArchons) return true;

		// hire a gardener once all available gardeners are already watering
		if(GameState.gardenerCount < rc.getTreeCount() * TREES_PER_GARDENER) return true;

		// hire a gardener if needed bullets per turn is greater than bullet income
		if(GameState.maxShotCostsPerTurn() > GameState.bulletIncome() && rc.getTeamBullets() > 200) return true;

		return false;
	}

}
