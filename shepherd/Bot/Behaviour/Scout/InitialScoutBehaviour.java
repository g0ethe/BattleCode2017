package shepherd.Bot.Behaviour.Scout;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.TreeInfo;
import shepherd.Bot.Scout;

public class InitialScoutBehaviour extends ScoutBehaviour {

	RobotController scout;

	ArrayList<MapLocation> neutralTreeLocations;

	public void execute() throws GameActionException {
		initialize();
		harvestBullets();
		Clock.yield();
	}


	private void harvestBullets() throws GameActionException {
		getNeutralBulletTrees();
		MapLocation nearestBulletTree = getNearestFromList(neutralTreeLocations);
		moveAndShakeTree(nearestBulletTree);
	}



	// scans surrounding neutral bullet trees and saves them in a list. also cleans list once a tree has been shaken
	private void getNeutralBulletTrees() throws GameActionException {
		TreeInfo[] nearbyNeutralTrees = scout.senseNearbyTrees(scout.getType().sensorRadius, Team.NEUTRAL);
		for(TreeInfo tree : nearbyNeutralTrees) {
			if(tree.containedBullets > 0) {
				if(!neutralTreeLocations.contains(tree.location)) neutralTreeLocations.add(tree.location);
			}
			else if(neutralTreeLocations.contains(tree.location)) {
				neutralTreeLocations.remove(tree.location);
			}
		}
	}



	// moves scout towards given location, or towards estimated enemy location if given location sucks
	private void moveAndShakeTree(MapLocation treeLocation) throws GameActionException {
		MapLocation targetLocation = (treeLocation == null) ? getEnemyLocation() : treeLocation;
		if(targetLocation == null) return;

		if(treeLocation != null && scout.canShake(treeLocation)) scout.shake(treeLocation);

		if(!scout.hasMoved()) {
			Direction dir = scout.getLocation().directionTo(targetLocation);
			for(int i = 0; i <= 120; i+=5) {
				if(scout.canMove(dir.rotateLeftDegrees(i))) { scout.move(dir.rotateLeftDegrees(i)); break; }
				if(scout.canMove(dir.rotateRightDegrees(i))) { scout.move(dir.rotateRightDegrees(i)); break; }
			}
		}

		if(treeLocation != null && scout.canShake(treeLocation)) scout.shake(treeLocation);
	}



	// returns estimated enemy location, or location of nearest enemy gardener, or location of nearest enemy archon
	private MapLocation getEnemyLocation() throws GameActionException {
		if(scout.getRoundNum() > getLastHostileSenseTurn()) {
			setNearbyHostileUnits(scout.senseNearbyRobots(scout.getType().sensorRadius, scout.getTeam().opponent()));
		}
		if(getNearbyHostileUnits().length == 0) return getAverageEnemyStartingLocaction();

		if(getNearestHostileGardenerOrArchon() != null) return getNearestHostileGardenerOrArchon().location;
		return getAverageEnemyStartingLocaction();
	}



	// small set-up
	private void initialize() throws GameActionException {
		if(scout == null) {
			neutralTreeLocations = getNeutralTreeLocations();
			scout = getController();
		}
	}


	// functions to hide ugly typecasts and stuff
	private ArrayList<MapLocation> getNeutralTreeLocations() {
		return ((Scout)executer).neutralTreeLocations;
	}
	private RobotController getController() {
		return ((Scout)executer).getController();
	}
	private MapLocation getNearestFromList(List<MapLocation> locationList) {
		return executer.getNearest(neutralTreeLocations);
	}
	private int getLastHostileSenseTurn() {
		return ((Scout)executer).lastHostileSenseTurn;
	}
	private void setNearbyHostileUnits(RobotInfo[] hostiles) {
		((Scout)executer).lastHostileSenseTurn = scout.getRoundNum();
		((Scout)executer).nearbyHostileUnits = hostiles;
	}
	private RobotInfo[] getNearbyHostileUnits() {
		return ((Scout)executer).nearbyHostileUnits;
	}
	private MapLocation getAverageEnemyStartingLocaction() {
		return ((Scout)executer).getAverageEnemyStartingLocaction();
	}
	private RobotInfo getNearestHostileGardenerOrArchon() throws GameActionException {
		return ((Scout)executer).getNearestHostileGardenerOrArchon();
	}

}
