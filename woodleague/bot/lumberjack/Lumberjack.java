package woodleague.bot.lumberjack;

import java.util.HashMap;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;
import woodleague.bot.Bot;
import woodleague.util.Broadcast;
import woodleague.util.GameState;
import woodleague.util.Geometry;

public class Lumberjack extends Bot {

	private static HashMap<Integer, MapLocation> previousLocations = new HashMap<Integer, MapLocation>();

	private static float c = 1, s = 1.5f, a = 2, v = 1;


	public static void run() throws GameActionException {
		while(true) {
			long turn = rc.getRoundNum();

			Broadcast.updateUnitCount();
			collectGoodieHuts();
			chargeEnemy();
			clearSpawnLocation();
			chopTowardsEnemy();

			if(turn == rc.getRoundNum()) Clock.yield();
		}
	}


	private static void chopTowardsEnemy() throws GameActionException {
		if(rc.hasMoved() && rc.hasAttacked()) return;

		// get tree closest to us, thats still about in the direction of the enemy's center
		MapLocation center = rc.getLocation().add(rc.getLocation().directionTo(GameState.enemyCenter), rc.getType().sensorRadius - Float.MIN_NORMAL);
		TreeInfo[] trees = rc.senseNearbyTrees(center, rc.getType().sensorRadius, Team.NEUTRAL);
		TreeInfo closest = null; float minDist = Float.MAX_VALUE;
		for(TreeInfo tree : trees) {
			float dist = tree.getLocation().distanceTo(rc.getLocation());
			if(tree.containedBullets > 0 && rc.canShake(tree.getID())) rc.shake(tree.getID());
			if(dist < minDist) {
				minDist = dist;
				closest = tree;
			}
		}

		// if no tree there, just move
		if(closest == null) {
			if(!rc.hasMoved()) {

				// get next target location to flock-sweep towards
				if(rc.getLocation().distanceTo(GameState.targetLocation) <= rc.getType().sensorRadius / 2) {
					int i = 0;
					for(; i < GameState.enemySpawns.length; i++) if(GameState.targetLocation.equals(GameState.enemySpawns[i])) break;
					if(i >= GameState.enemySpawns.length - 1) i = 0;
					else i = i + 1;
					GameState.targetLocation = GameState.enemySpawns[i];
				}

				flockMove(c, s, a, v, GameState.targetLocation);

			}
			return;
		}

		// otherwise if can chop tree, do so
		if(rc.canChop(closest.getID())) {
			if(closest.containedBullets > 0 && rc.canShake(closest.getID()) ) rc.shake(closest.getID());
			rc.chop(closest.getID());
			return;
		}

		// move towards that tree
		if(!rc.hasMoved()) tryMove(rc.getLocation().directionTo(closest.getLocation()), 120, 5);
		if(rc.canChop(closest.getID())) {
			if(closest.containedBullets > 0 && rc.canShake(closest.getID())) rc.shake(closest.getID());
			rc.chop(closest.getID());
		}
	}


	private static void chargeEnemy() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam().opponent());
		TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius, rc.getTeam().opponent());
		if(enemies.length == 0 && trees.length == 0) return;

		// go chop enemy's trees
		if(enemies.length == 0) {
			TreeInfo tree = trees[0];
			if(rc.canChop(tree.getID())) rc.chop(tree.getID());
			else if(!rc.hasMoved()) tryMove(rc.getLocation().directionTo(tree.getLocation()), 120, 5);
			if(rc.canChop(tree.getID())) rc.chop(tree.getID());
			return;
		}

		RobotType type = enemies[0].getType();

		// check if im one of the attacking lumberjacks
		float distance = rc.getLocation().distanceTo(enemies[0].getLocation()) - type.bodyRadius;
		if(distance <= GameConstants.LUMBERJACK_STRIKE_RADIUS) {
			// im an attacker

			// do i have to move?
			RobotInfo[] nearbyAllies = rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS + Float.MIN_NORMAL, rc.getTeam());
			if(nearbyAllies.length == 0) {
				// nothing near, can attack
				if(rc.canStrike()) rc.strike();
			}
			else {
				// i should try to move a bit, to be out of range of my allies
				float dx = 0, dy = 0;
				Direction dir = nearbyAllies[0].getLocation().directionTo(rc.getLocation());
				dx += dir.getDeltaX(1);
				dy += dir.getDeltaY(1);
				dir = rc.getLocation().directionTo(enemies[0].getLocation());
				dx += dir.getDeltaX(1);
				dy += dir.getDeltaY(1);
				dx /= 2; dy /= 2;
				dir = new Direction(dx, dy);
				if(!rc.hasMoved()) tryMove(dir, 120, 5);
				nearbyAllies = rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS + Float.MIN_NORMAL, rc.getTeam());
				if(nearbyAllies.length == 0 && rc.canStrike()) rc.strike();
			}
		}
		else {
			// look for attackers
			float maxAttackers = (type == RobotType.ARCHON || type == RobotType.TANK) ? 4 : 3;
			RobotInfo[] allies = rc.senseNearbyRobots(enemies[0].getLocation(), type.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam());
			if(allies.length >= maxAttackers) {
				// i should stay away
				Direction dir = enemies[0].getLocation().directionTo(rc.getLocation());
				if(!rc.hasMoved()) tryMove(dir, 85, 5);
			}
			else {
				// i should attack
				Direction dir = enemies[0].getLocation().directionTo(rc.getLocation()).opposite();
				if(!rc.hasMoved()) tryMove(dir, 120, 5);
				// can i safely attack at the moment?
				RobotInfo[] nearbyAllies = rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS + Float.MIN_NORMAL, rc.getTeam());
				if(nearbyAllies.length == 0 && rc.canStrike()) rc.strike();
			}
		}

	}


	private static void collectGoodieHuts() throws GameActionException {

		// find goodie huts
		TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius, Team.NEUTRAL);
		if(trees.length == 0) return;
		TreeInfo goodieHut = null;
		for(TreeInfo tree : trees) {
			if(tree.containedRobot != null) { goodieHut = tree; break; }
		}
		if(goodieHut == null) return;

		// try collecting goodies from hut
		if(rc.canChop(goodieHut.getID())) {
			if(rc.canShake(goodieHut.getID())) rc.shake(goodieHut.getID());
			rc.chop(goodieHut.getID());
			return;
		}

		// find nearest tree blocking the direct path to the goodies
		MapLocation M = rc.getLocation();
		Direction dir = M.directionTo(goodieHut.getLocation());
		float h = rc.getType().bodyRadius + Float.MIN_NORMAL;
		TreeInfo blocksPath = null;
		for(TreeInfo tree : trees) {
			if(Geometry.isInRectangle(tree, dir, M, h)) { blocksPath = tree; break; }
		}

		// if no tree is in the way, just move straight to goodie hut
		if(blocksPath == null && !rc.hasMoved()) tryMove(dir, 120, 5);
		if(rc.canChop(goodieHut.getID())) {
			if(rc.canShake(goodieHut.getID())) rc.shake(goodieHut.getID());
			rc.chop(goodieHut.getID());
			return;
		}

		// if you already moved, try to swing your axe in the nearest trees face
		if(blocksPath == null && rc.hasMoved() && !rc.hasAttacked()) {
			if(rc.canShake(trees[0].getID())) rc.shake(trees[0].getID());
			if(rc.canChop(trees[0].getID())) rc.chop(trees[0].getID());
		}

		// move in the direction of the goodie hut, towards the blocking tree and chop it
		if(blocksPath != null) {

			// calculate distance to tree on direction to goodies
			Direction toBlocking = M.directionTo(blocksPath.getLocation());
			float alpha = dir.radiansBetween(toBlocking);
			float d = M.distanceTo(blocksPath.getLocation());
			float x = (float) Math.abs(d * Math.cos(alpha));
			float l = (float) Math.abs(d * Math.sin(alpha));
			float r2 = (blocksPath.getRadius() + rc.getType().bodyRadius) * (blocksPath.getRadius() + rc.getType().bodyRadius);
			float y = (float) Math.sqrt(r2 - l*l);
			float length = x - y;

			// try to move to tree
			if(length > 0 && !rc.hasMoved()) {
				if(rc.canMove(dir, length)) rc.move(dir, length);
			}
			if(rc.canChop(blocksPath.getID())) {
				if(rc.canShake(blocksPath.getID())) rc.shake(blocksPath.getID());
				rc.chop(blocksPath.getID());
			}
		}

	}


	private static void clearSpawnLocation() throws GameActionException {

		// check spawn location for trees to chop to make some space for a farm later on
		float distToSpawn = rc.getLocation().distanceTo(GameState.mySpawn);
		if(distToSpawn <= rc.getType().sensorRadius) {
			TreeInfo[] trees = rc.senseNearbyTrees(GameState.mySpawn, rc.getType().sensorRadius, Team.NEUTRAL);
			if(trees.length == 0) return;
			if(trees[0].containedBullets > 0 && rc.canShake(trees[0].getID())) rc.shake(trees[0].getID());
			if(rc.canChop(trees[0].getID())){ rc.chop(trees[0].getID()); return; }
			if(!rc.hasMoved()) tryMove(rc.getLocation().directionTo(trees[0].getLocation()), 120, 5);
			if(trees[0].containedBullets > 0 && rc.canShake(trees[0].getID())) rc.shake(trees[0].getID());
			if(rc.canChop(trees[0].getID())) rc.chop(trees[0].getID());
		}

	}


	private static void flockMove(float c, float s, float a, float v, MapLocation target) throws GameActionException {
		// precalculations for alignment, cohesion, and seperation
		RobotInfo[] alliedRobots = rc.senseNearbyRobots(rc.getType().sensorRadius, rc.getTeam());
		Direction[] alignmentDirs = new Direction[alliedRobots.length];
		Direction[] separationDirs = new Direction[alliedRobots.length];
		float[] separationWeights = new float[alliedRobots.length];
		float[] alignmentWeigths = new float[alliedRobots.length];
		float x = 0, y = 0; int i = 0;
		for(RobotInfo robot : alliedRobots) {
			if(robot.getType() != RobotType.LUMBERJACK) continue;

			// cohesion
			MapLocation current = robot.getLocation();
			x += current.x; y += current.y;

			// separation
			Direction seperate = current.directionTo(rc.getLocation());
			separationDirs[i] = seperate;
			float dist = current.distanceTo(rc.getLocation());
			float weight = 1/(dist - 2*rc.getType().bodyRadius + 0.01f);
			separationWeights[i] = weight;

			// alignment
			MapLocation previous = previousLocations.put(robot.getID(), current);
			if(previous != null) {
				Direction alignment = previous.directionTo(current);
				weight = previous.distanceTo(current);
				alignmentDirs[i] = alignment;
				alignmentWeigths[i] = weight;
			}

			i++;
		}
		MapLocation center = (i == 0)? null : new MapLocation(x/i, y/i);

		// calculate flocking directions
		float cx = 0, cy = 0, sx = 0, sy = 0, ax = 0, ay = 0, vx = 0, vy = 0;
		for(i = 0; i < alignmentDirs.length; i++) {
			Direction dir = alignmentDirs[i];
			if(dir == null) continue;
			ax += dir.getDeltaX(alignmentWeigths[i]);
			ay += dir.getDeltaY(alignmentWeigths[i]);
		}
		for(i = 0; i < separationDirs.length; i++) {
			Direction dir = separationDirs[i];
			if(dir == null) continue;
			sx += dir.getDeltaX(separationWeights[i]);
			sy += dir.getDeltaY(separationWeights[i]);
		}
		if(center != null) {
			Direction dir = rc.getLocation().directionTo(center);
			float dist = rc.getLocation().distanceTo(center);
			cx += dir.getDeltaX(dist);
			cy += dir.getDeltaY(dist);
		}
		if(target != null) {
			Direction dir = rc.getLocation().directionTo(target);
			float dist = rc.getLocation().distanceTo(target);
			vx += dir.getDeltaX(dist);
			vy += dir.getDeltaY(dist);
		}

		// calculate flocking force
		cx *= c; cy *= c; sx *= s; sy *= s; ax *= a; ay *= a; vx *= v; vy *= v;
		float dx = (cx + sx + ax + vx) / 4;
		float dy = (cy + sy + ay + vy) / 4;
		Direction flockingDirection = new Direction(dx, dy);

		tryMove(flockingDirection, 120, 5);
	}

}










