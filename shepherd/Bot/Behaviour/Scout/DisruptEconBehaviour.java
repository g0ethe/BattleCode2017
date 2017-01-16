package shepherd.Bot.Behaviour.Scout;

import java.util.ArrayList;

import battlecode.common.BodyInfo;
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

	private void debug(Object disruptionTarget) throws GameActionException {
		if(disruptionTarget == null) System.out.println("Null.");
		else if(disruptionTarget instanceof RobotInfo) System.out.println("Target is Robot: " + ((RobotInfo)disruptionTarget).ID);
		else if(disruptionTarget instanceof TreeInfo) System.out.println("Target is Tree: " + ((TreeInfo)disruptionTarget).ID);
		else if(disruptionTarget instanceof MapLocation) System.out.println("Target is Location: (" + ((MapLocation)disruptionTarget).x + ", " + ((MapLocation)disruptionTarget).y);
		else System.out.println("Wat.");

		if(disruptionTarget != null) {
			MapLocation targetLocation = null;
			if(disruptionTarget instanceof MapLocation) targetLocation = ((MapLocation)disruptionTarget);
			else if(disruptionTarget instanceof BodyInfo) targetLocation = ((BodyInfo)disruptionTarget).getLocation();

			if(targetLocation != null) scout.setIndicatorLine(scout.getLocation(), targetLocation, 255, 0, 0);
		}
	}


	ArrayList<Integer> turnUpdatedArchon = new ArrayList<Integer>(10);
	ArrayList<Integer> turnUpdatedGardener = new ArrayList<Integer>(10);
	ArrayList<Integer> savedArchonIDs = new ArrayList<Integer>(10);
	ArrayList<Integer> savedGardenerIDs = new ArrayList<Integer>(10);
	ArrayList<MapLocation> savedArchonLocations = new ArrayList<MapLocation>(10);
	ArrayList<MapLocation> savedGardenerLocations = new ArrayList<MapLocation>(10);


	public void execute() throws GameActionException {
		initialize();

		// gets robot info, tree info, or map location as a target to find enemy gardeners
		Object disruptionTarget = getDistruptionTarget();
		debug(disruptionTarget);

		// TODO: 1. move towards disruption target, while trying to avoid getting damaged
		//		 2. attack gardener, if a hit is (almost) guaranteed
		//		 3. if can still move, move in a way, such that bullets fired at this scout
		//			will likely travel in the direction of an opponents unit

		cleanOutdatedInformation();
		Clock.yield();
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
