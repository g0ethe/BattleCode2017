package shepherd.Bot.Behaviour.Scout;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.BodyInfo;
import battlecode.common.BulletInfo;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;
import shepherd.Bot.Utilities.Geometry;
import shepherd.Bot.Utilities.Util;


/*
 * Goal: Find and kill as many hostile gardeners as possible, while trying to stay alive
 * (and gathering and broadcasting information about opponents units, maybe)
 */

public class DisruptEconBehaviour extends ScoutBehaviour {

	ArrayList<Integer> turnUpdatedArchon = new ArrayList<Integer>(10);
	ArrayList<Integer> turnUpdatedGardener = new ArrayList<Integer>(10);
	ArrayList<Integer> savedArchonIDs = new ArrayList<Integer>(10);
	ArrayList<Integer> savedGardenerIDs = new ArrayList<Integer>(10);
	ArrayList<MapLocation> savedArchonLocations = new ArrayList<MapLocation>(10);
	ArrayList<MapLocation> savedGardenerLocations = new ArrayList<MapLocation>(10);


	public void execute() throws GameActionException {
		initialize();
		Object disruptionTarget = getDistruptionTarget();
		moveTowardsTargetAvoidingDamage(disruptionTarget);

		//TODO:	2. attack gardener, if a hit is (almost) guaranteed
		//		 3. if can still move, move in a way, such that bullets fired at this scout
		//			will likely travel in the direction of an opponents unit

		cleanOutdatedInformation();
		Clock.yield();
	}


	/*
	 * moves towards given target, while trying to avoid taking damage
	 *
	 * by staying out of range (stride + attack) of hostile lumbers,
	 * and by dodging (at least some) bullets.
	 *
	 */
	private void moveTowardsTargetAvoidingDamage(Object target) throws GameActionException {
		MapLocation goal = (target instanceof MapLocation) ? (MapLocation)target : (target instanceof BodyInfo) ? ((BodyInfo)target).getLocation() : null;
		System.out.println("Goal: " + goal); // debug info to console

		// sense bullets that might hit us at the end of this round
		BulletInfo[] possiblyHittingBullets = getPossiblyHittingBullets();
		for(BulletInfo bullet : possiblyHittingBullets) System.out.println("Bullet: " + bullet.ID); // debug info to console

		// sense nearby hostiles that could still attack us this round
		List<RobotInfo> possiblyAttackingHostiles = getPossiblyAttackingHostiles();
		for(RobotInfo robot : possiblyAttackingHostiles) System.out.println("Robot:  " + robot.ID); // debug info to console

		// TODO:
		// find direction which will dodge as many bullets as possible,
		// which will keep us out of range of as many hostile units as possible,
		// and which will move us closer towards our goal location
	}


	/*
	 * returns enemy units that are in attack range
	 */
	private List<RobotInfo> getPossiblyAttackingHostiles() throws GameActionException {

		// get all hostile units in sensor range
		RobotInfo[] hostiles = senseHostileRobots();
		ArrayList<RobotInfo> dangerZoneEnemies = new ArrayList<RobotInfo>();

		// check if enemy can attack at all and, if so,
		// check if it could attack us if we happen to move in its direction
		for(RobotInfo enemy : hostiles) {
			float attackRange = Util.getMaxAttackRange(enemy.type);
			if(attackRange > 0) {
				float distance = Geometry.distanceBetween(scout.getLocation(), scout.getType().bodyRadius, enemy);
				float maxMoveThisTurn = (scout.hasMoved()) ? 0 : scout.getType().strideRadius;
				if(distance - maxMoveThisTurn <= attackRange) dangerZoneEnemies.add(enemy);
			}
		}

		// return all units that could hurt us if we walk incorrectly
		return dangerZoneEnemies;
	}


	/*
	 * returns all bullets that might hit scout this turn,
	 * taking into consideration scouts maximum stride radius,
	 * a bullets maximum speed, a scouts body radius,
	 * and the minimum positive offset
	 * (conservative calculation)
	 */
	private BulletInfo[] getPossiblyHittingBullets() throws GameActionException {
		float myMaxRadius = scout.getType().bodyRadius + scout.getType().strideRadius;
		float maxBulletSpeed = Util.getMaxBulletSpeed();
		float maxBulletSensorRadius = Float.MIN_NORMAL + myMaxRadius + maxBulletSpeed;
		if(maxBulletSensorRadius > scout.getType().bulletSightRadius) maxBulletSensorRadius = scout.getType().bulletSightRadius;

		return scout.senseNearbyBullets(maxBulletSensorRadius);
	}


	/*
	 * tries to find a fitting target to scout, stalk, and attack.
	 *
	 * tries to find gardeners first, by sensing nearby units,
	 * by looking for opponent's trees (gardeners will likely water those),
	 * by memorizing already sensed gardeners' locations,
	 *
	 * by looking for hostile archons (will build gardeners at some point),
	 * by memorizing already sensed archons' locations,
	 *
	 *
	 * should all of those not give something to scout, get nearest enemy spawning point
	 */
	private Object getDistruptionTarget() throws GameActionException {

		// get hostile gardener with lowest health as target
		// and memorize locations and IDs of sensed gardeners and archons
		RobotInfo[] nearbyHostileRobots = senseHostileRobots();
		saveArchonGardenerLocations(nearbyHostileRobots);
		RobotInfo lowestGardener = Util.getLowestOfTypeFromList(RobotType.GARDENER, nearbyHostileRobots);
		if(lowestGardener != null) return lowestGardener;

		// get nearest hostile bullet tree, to find gardener there
		TreeInfo[] nearbyHostileTrees = senseHostileTrees();
		TreeInfo nearestHostileTree = Geometry.getNearest(nearbyHostileTrees, scout.getLocation());
		if(nearestHostileTree != null) return nearestHostileTree;

		// get nearest location from already sensed gardener locations
		MapLocation nearestGardenerLocation = Geometry.getNearest(savedGardenerLocations, scout.getLocation());
		if(nearestGardenerLocation != null) return nearestGardenerLocation;

		// get any hostile archon that has been sensed this turn
		RobotInfo lowestArchon = Util.getLowestOfTypeFromList(RobotType.ARCHON, nearbyHostileRobots);
		if(lowestArchon != null) return lowestArchon;

		// get nearest location from already sensed archon locations
		MapLocation nearestArchonLocation = Geometry.getNearest(savedArchonLocations, scout.getLocation());
		if(nearestArchonLocation != null) return nearestArchonLocation;

		// get the nearest spawning point
		MapLocation nearestSpawnLocation = Geometry.getNearest(scout.getInitialArchonLocations(scout.getTeam().opponent()), scout.getLocation());
		return nearestSpawnLocation;
	}


	/*
	 * saves locations and IDs of archons and gardeners in the given robot info array.
	 * updates locations, if an ID has already been saved.
	 * also saves the turn number of the last update of the saved information
	 * (for later clean-up - we don't actually want too old information)
	 */
	private void saveArchonGardenerLocations(RobotInfo[] robots) {
		for(RobotInfo robot : robots) {
			// check if we already have an archon with this id
			// if not, save it to list
			// otherwise update saved location
			if(robot.getType() == RobotType.ARCHON) {
				int index = savedArchonIDs.indexOf(robot.getID());
				if(index == -1) {
					savedArchonIDs.add(robot.getID());
					savedArchonLocations.add(robot.getLocation());
					turnUpdatedArchon.add(scout.getRoundNum());
				}
				else {
					savedArchonLocations.set(index, robot.getLocation());
					turnUpdatedArchon.set(index, scout.getRoundNum());
				}
			}

			// check if we already have a gardener with this id
			// if not, save it to list
			// otherwise update saved location
			if(robot.getType() == RobotType.GARDENER) {
				int index = savedGardenerIDs.indexOf(robot.getID());
				if(index == -1) {
					savedGardenerIDs.add(robot.getID());
					savedGardenerLocations.add(robot.getLocation());
					turnUpdatedGardener.add(scout.getRoundNum());
				}
				else{
					savedGardenerLocations.set(index, robot.getLocation());
					turnUpdatedGardener.set(index, scout.getRoundNum());
				}
			}
		}
	}


	/*
	 * removes stored information about sensed archons and gardeners,
	 * once the information can be assumed to be out of date,
	 * i.e. once the last update was 20 turns or more before,
	 * or the saved robot can be assumed to be destroyed
	 * (when cannot sense robot, although given its speed it should be sensed)
	 */
	private void cleanOutdatedInformation() throws GameActionException {
		int maxSaveTurns = 20;
		ArrayList<Integer> indices = new ArrayList<Integer>();

		// clean all information about archons that is older than maxSaveTurns
		for(int index = 0; index < turnUpdatedArchon.size(); index++)
			if(turnUpdatedArchon.get(index) + maxSaveTurns < scout.getRoundNum()) indices.add(index);
		for(int index : indices) {
			turnUpdatedArchon.remove(index);
			savedArchonIDs.remove(index);
			savedArchonLocations.remove(index);
		}
		indices.clear();

		// clean all information about gardeners that is older than maxSaveTurns
		for(int index = 0; index < turnUpdatedGardener.size(); index++)
			if(turnUpdatedGardener.get(index) + maxSaveTurns < scout.getRoundNum()) indices.add(index);
		for(int index : indices) {
			turnUpdatedGardener.remove(index);
			savedGardenerIDs.remove(index);
			savedGardenerLocations.remove(index);
		}
		indices.clear();

		// if saved gardener can be assumed to be destroyed, clean stored data
		for(int index = 0; index < savedGardenerLocations.size(); index++) {
			MapLocation gardenerLocation = savedGardenerLocations.get(index);
			if(scout.canSenseLocation(gardenerLocation)) {
				int turnsSince = scout.getRoundNum() - turnUpdatedGardener.get(index);
				float maxMoveDistance = turnsSince * RobotType.GARDENER.strideRadius;
				if(scout.canSenseAllOfCircle(gardenerLocation, maxMoveDistance) && !scout.canSenseRobot(savedGardenerIDs.get(index))) indices.add(index);
			}
		}
		for(int index : indices) {
			turnUpdatedGardener.remove(index);
			savedGardenerIDs.remove(index);
			savedGardenerLocations.remove(index);
		}
		indices.clear();

		// if saved archon can be assumed to be destroyed, clean stored data
		for(int index = 0; index < savedArchonLocations.size(); index++) {
			MapLocation archonLocation = savedArchonLocations.get(index);
			if(scout.canSenseLocation(archonLocation)) {
				int turnsSince = scout.getRoundNum() - turnUpdatedArchon.get(index);
				float maxMoveDistance = turnsSince * RobotType.ARCHON.strideRadius;
				if(scout.canSenseAllOfCircle(archonLocation, maxMoveDistance) && !scout.canSenseRobot(savedArchonIDs.get(index))) indices.add(index);
			}
		}
		for(int index : indices) {
			turnUpdatedArchon.remove(index);
			savedArchonIDs.remove(index);
			savedArchonLocations.remove(index);
		}
	}



}
