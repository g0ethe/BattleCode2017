package woodleague.bot.gardener;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotType;
import woodleague.bot.Bot;
import woodleague.util.Broadcast;
import woodleague.util.GameState;

public class Gardener extends Bot {

	public enum Buildable {
		TREE, LUMBERJACK, SCOUT, SOLDIER, TANK
	}

	public static void run() throws GameActionException {
		while(true) {
			long turn = rc.getRoundNum();

			Broadcast.updateUnitCount();
			Buildable toBuild = getToBuild();
			build(toBuild);

			if(turn == rc.getRoundNum()) Clock.yield();
		}
	}

	private static Buildable getToBuild() throws GameActionException {

		// TODO build order
		if(GameState.canWinByPoints()) rc.donate(rc.getTeamBullets());
		return Buildable.LUMBERJACK;
	}

	private static void build(Buildable type) throws GameActionException {
		switch(type) {
		case TREE: plantTree(); break;
		case LUMBERJACK: build(RobotType.LUMBERJACK); break;
		case SCOUT: build(RobotType.SCOUT); break;
		case SOLDIER: build(RobotType.SOLDIER); break;
		case TANK: build(RobotType.TANK); break;
		}
	}

	private static void plantTree() throws GameActionException {
		Direction dir = rc.getLocation().directionTo(GameState.enemyCenter);
		for(int i = 0; i <= 180; i += 6) {
			if(rc.canPlantTree(dir.rotateLeftDegrees(i))) { rc.plantTree(dir.rotateLeftDegrees(i)); break; }
			if(rc.canPlantTree(dir.rotateRightDegrees(i))) { rc.plantTree(dir.rotateRightDegrees(i)); break; }
		}
	}

	private static void build(RobotType type) throws GameActionException {
		Direction dir = rc.getLocation().directionTo(GameState.enemyCenter);
		for(int i = 0; i <= 180; i += 6) {
			if(rc.canBuildRobot(type, dir.rotateLeftDegrees(i))) { rc.buildRobot(type, dir.rotateLeftDegrees(i)); break; }
			if(rc.canBuildRobot(type, dir.rotateRightDegrees(i))) { rc.buildRobot(type, dir.rotateRightDegrees(i)); break; }
		}
	}

}
