package woodleague.bot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import woodleague.util.Broadcast;
import woodleague.util.GameState;
import woodleague.util.Geometry;
import woodleague.util.Util;

public class Bot {

	public static RobotController rc;

	public static void initialize(RobotController bot) {
		rc = bot;
		Util.rc = rc;
		Geometry.rc = rc;
		Broadcast.rc = rc;
		GameState.rc = rc;
		GameState.mySpawn = rc.getLocation();
		GameState.spawnRound = rc.getRoundNum();
		GameState.ourSpawns = rc.getInitialArchonLocations(rc.getTeam());
		GameState.ourCenter = Geometry.centerOf(GameState.ourSpawns);
		GameState.enemySpawns = rc.getInitialArchonLocations(rc.getTeam().opponent());
		GameState.enemyCenter = Geometry.centerOf(GameState.enemySpawns);
		GameState.startArchons = GameState.ourSpawns.length;
		GameState.targetLocation = GameState.enemyCenter;
	}


	public static void tryMove(Direction dir, float maxDegrees, float turnDegrees) throws GameActionException {
		if(dir == null || (dir.getDeltaX(1) == 0 && dir.getDeltaY(1) == 0)) return;
		for(float i = 0; i <= maxDegrees; i += turnDegrees) {
			if(rc.canMove(dir.rotateLeftDegrees(i))) { rc.move(dir.rotateLeftDegrees(i)); break; }
			if(rc.canMove(dir.rotateRightDegrees(i))) { rc.move(dir.rotateRightDegrees(i)); break; }
		}
	}


}
