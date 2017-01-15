package shepherd.Bot.Behaviour.Scout;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.BodyInfo;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;

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
		RobotInfo lowestGardener = getLowestOfTypeFromList(RobotType.GARDENER, nearbyHostileRobots);
		if(lowestGardener != null) return lowestGardener;

		// get nearest hostile bullet tree, to find gardener there
		TreeInfo[] nearbyHostileTrees = senseHostileTrees();
		TreeInfo nearestHostileTree = getNearest(nearbyHostileTrees, scout.getLocation());
		if(nearestHostileTree != null) return nearestHostileTree;

		// get nearest location from already sensed gardener locations
		MapLocation nearestGardenerLocation = getNearest(savedGardenerLocations, scout.getLocation());
		if(nearestGardenerLocation != null) return nearestGardenerLocation;

		// get any hostile archon that has been sensed this turn
		RobotInfo lowestArchon = getLowestOfTypeFromList(RobotType.ARCHON, nearbyHostileRobots);
		if(lowestArchon != null) return lowestArchon;

		// get nearest location from already sensed archon locations
		MapLocation nearestArchonLocation = getNearest(savedArchonLocations, scout.getLocation());
		if(nearestArchonLocation != null) return nearestArchonLocation;

		// get the nearest spawning point
		MapLocation nearestSpawnLocation = getNearest(scout.getInitialArchonLocations(scout.getTeam().opponent()), scout.getLocation());
		return nearestSpawnLocation;
	}


	/*
	 * returns the robot with the lowest amount of health of the given type from given array
	 */
	private RobotInfo getLowestOfTypeFromList(RobotType type, RobotInfo[] robots) {
		RobotInfo lowest = null;
		double minHP = Double.MAX_VALUE;
		for(RobotInfo robot : robots) {
			if(robot.getType() == type && robot.getHealth() < minHP) {
				lowest = robot;
				minHP = robot.getHealth();
			}
		}
		return lowest;
	}


	/*
	 * returns the nearest thing from given list or array to the relative location
	 */
	private MapLocation getNearest(List<MapLocation> locations, MapLocation relativeLocation) throws GameActionException {
		float minDist = Float.MAX_VALUE;
		MapLocation nearest = null;
		for(int index = 0; index < locations.size(); index++) {
			MapLocation loc = locations.get(index);
			float dist = relativeLocation.distanceTo(loc);
			if(dist < minDist) {
				nearest = loc;
				minDist = dist;
			}
		}
		return nearest;
	}
	private TreeInfo getNearest(TreeInfo[] trees, MapLocation relativeLocation) throws GameActionException {
		float minDist = Float.MAX_VALUE;
		TreeInfo nearest = null;
		for(TreeInfo tree : trees) {
			float dist = relativeLocation.distanceTo(tree.location);
			if(dist < minDist) {
				nearest = tree;
				minDist = dist;
			}
		}
		return nearest;
	}
	private MapLocation getNearest(MapLocation[] locations, MapLocation relativeLocation) throws GameActionException {
		float minDist = Float.MAX_VALUE;
		MapLocation nearest = null;
		for(MapLocation loc : locations) {
			float dist = relativeLocation.distanceTo(loc);
			if(dist < minDist) {
				nearest = loc;
				minDist = dist;
			}
		}
		return nearest;
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



}
