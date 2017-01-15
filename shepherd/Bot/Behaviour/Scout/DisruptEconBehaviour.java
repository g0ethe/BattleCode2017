package shepherd.Bot.Behaviour.Scout;



import battlecode.common.BodyInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;



/*
 * Should try to stay on trees and close to enemy gardeners, while shooting the gardener
 * and dodging incoming bullets in a way that they hit the enemy units instead of self.
 *
 * Shooting the gardener should be a guaranteed hit, to avoid wasting bullets, which from a scout
 * are slow and deal only little damage, so we have to stay as close to gardeners as possible.
 */

public class DisruptEconBehaviour extends ScoutBehaviour {

	public void execute() throws GameActionException {
		initialize();

		BodyInfo targetToDisrupt = getDisruptionTarget();
		attackMove(targetToDisrupt);

		Clock.yield();
	}

	private BodyInfo getDisruptionTarget() throws GameActionException {
		RobotInfo killableRobot = getKillableHostileRobot();
		if(killableRobot != null) return killableRobot;

		TreeInfo killableTree = getKillableHostileTree();
		if(killableTree != null) return killableTree;

		RobotInfo enemyGardener = getHostileGardener();
		if(enemyGardener != null) return enemyGardener;

		return null;
	}



	private RobotInfo getKillableHostileRobot() throws GameActionException {
		RobotInfo[] hostileRobots = senseHostileRobots();
		for(RobotInfo enemy : hostileRobots)
			if(enemy.health <= scout.getType().attackPower) return enemy;
		return null;
	}



	private TreeInfo getKillableHostileTree() throws GameActionException {
		TreeInfo[] hostileTrees = senseHostileTrees();
		for(TreeInfo tree : hostileTrees)
			if(tree.getHealth() <= scout.getType().attackPower + GameConstants.BULLET_TREE_DECAY_RATE * turnsUntilAttack(tree)) return tree;
		return null;
	}


	private int turnsUntilAttack(BodyInfo body) throws GameActionException {
		MapLocation bodyLocation = body.getLocation();
		MapLocation scoutLocation = scout.getLocation();
		Direction dirToBody = scoutLocation.directionTo(bodyLocation);
		MapLocation bulletSpawnLocation = scoutLocation.add(dirToBody, scout.getType().bodyRadius + GameConstants.BULLET_SPAWN_OFFSET);
		MapLocation bulletHitLocation = bodyLocation.subtract(dirToBody, body.getRadius());
		float distance = bulletSpawnLocation.distanceTo(bulletHitLocation);
		float bulletSpeed = scout.getType().bulletSpeed;
		int turnsToHit = (int)(distance/bulletSpeed + 0.5f);
		return turnsToHit;
	}


	private RobotInfo getHostileGardener() throws GameActionException {
		RobotInfo[] hostileRobots = senseHostileRobots();
		for(RobotInfo enemy : hostileRobots)
			if(enemy.getType() == RobotType.GARDENER) return enemy;
		return null;
	}


	// move towards targets location, avoiding attacks
	private void attackMove(BodyInfo target) throws GameActionException {
		if(target == null) return;

		if(tooFarToHitThisTurn(target)) moveAvoiding(target.getLocation());

		if(canHit(target)) shoot(target);
	}



	private boolean tooFarToHitThisTurn(BodyInfo target) throws GameActionException {
		Direction dirToTarget = scout.getLocation().directionTo(target.getLocation());
		MapLocation nearestBorderPointSelf = scout.getLocation().add(dirToTarget, scout.getType().bodyRadius);
		MapLocation nearestBorderPointTarget = target.getLocation().subtract(dirToTarget, target.getRadius());
		float distanceBetweenBorders = nearestBorderPointSelf.distanceTo(nearestBorderPointTarget);
		float maxAttackDistance = scout.getType().bulletSpeed + GameConstants.BULLET_SPAWN_OFFSET - Float.MIN_NORMAL;
		return distanceBetweenBorders > maxAttackDistance;
	}



	// TODO
	private void moveAvoiding(MapLocation targetLocation) throws GameActionException {
		if(scout.hasMoved()) return;

		Direction dir = scout.getLocation().directionTo(targetLocation);
		for(int i = 0; i <= 120; i++) {
			if(scout.canMove(dir.rotateLeftDegrees(i))) { scout.move(dir.rotateLeftDegrees(i)); return; }
			if(scout.canMove(dir.rotateRightDegrees(i))) { scout.move(dir.rotateRightDegrees(i)); return; }
		}
	}


	// TODO
	private boolean canHit(BodyInfo target) throws GameActionException {
		return true;
	}


	// TODO
	private void shoot(BodyInfo target) throws GameActionException {
		if(!scout.canFireSingleShot()) return;
		scout.fireSingleShot(scout.getLocation().directionTo(target.getLocation()));
	}

}
