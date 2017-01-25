package woodleague.bot.gardener;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;
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
			water();

			if(turn == rc.getRoundNum()) Clock.yield();
		}
	}

	private static void water() throws GameActionException {
		TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius, rc.getTeam());
		if(trees.length != 0) {
			for(TreeInfo tree : trees) {
				if(rc.canWater(tree.getID()) && tree.getHealth() <= tree.getMaxHealth() - GameConstants.WATER_HEALTH_REGEN_RATE)
					rc.water(tree.getID());
			}
		}

	}

	private static int index = 0;
	private static Buildable getToBuild() throws GameActionException {

		// TODO build order
		Buildable[] buildOrder = new Buildable[]{
				Buildable.SCOUT,
				Buildable.SCOUT,
				Buildable.SCOUT,
				Buildable.LUMBERJACK,
				Buildable.LUMBERJACK,
				Buildable.LUMBERJACK,
				Buildable.TREE,
				Buildable.LUMBERJACK,
				Buildable.TREE,
				Buildable.TREE,
				Buildable.LUMBERJACK,
				Buildable.LUMBERJACK,
				Buildable.LUMBERJACK};
		if(GameState.canWinByPoints()) rc.donate(rc.getTeamBullets());
		if(index == buildOrder.length) index = 0;
		return buildOrder[index];
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
			if(rc.canPlantTree(dir.rotateLeftDegrees(i))) { rc.plantTree(dir.rotateLeftDegrees(i)); index++; break; }
			if(rc.canPlantTree(dir.rotateRightDegrees(i))) { rc.plantTree(dir.rotateRightDegrees(i)); index++; break; }
		}
	}

	private static void build(RobotType type) throws GameActionException {
		Direction dir = rc.getLocation().directionTo(GameState.enemyCenter);
		for(int i = 0; i <= 180; i += 6) {
			if(rc.canBuildRobot(type, dir.rotateLeftDegrees(i))) { rc.buildRobot(type, dir.rotateLeftDegrees(i)); index++; break; }
			if(rc.canBuildRobot(type, dir.rotateRightDegrees(i))) { rc.buildRobot(type, dir.rotateRightDegrees(i)); index++; break; }
		}
	}

}
