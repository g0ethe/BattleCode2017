package shepherd.Bot.Behaviour.Scout;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import shepherd.Bot.Scout;
import shepherd.Bot.Behaviour.Behaviour;

public abstract class ScoutBehaviour extends Behaviour {

	protected ArrayList<MapLocation> neutralTreeLocations;
	protected RobotController scout;

	// functions to hide ugly typecasts and stuff
	// small set-up
	protected void initialize() throws GameActionException {
		if(scout == null) {
			neutralTreeLocations = getNeutralTreeLocations();
			scout = getController();
		}
	}
	protected ArrayList<MapLocation> getNeutralTreeLocations() {
		return ((Scout)executer).neutralTreeLocations;
	}
	protected RobotController getController() {
		return ((Scout)executer).getController();
	}
	protected MapLocation getNearestFromList(List<MapLocation> locationList) {
		return executer.getNearest(neutralTreeLocations);
	}
	protected int getLastHostileSenseTurn() {
		return ((Scout)executer).lastHostileSenseTurn;
	}
	protected void setNearbyHostileUnits(RobotInfo[] hostiles) {
		((Scout)executer).lastHostileSenseTurn = getController().getRoundNum();
		((Scout)executer).nearbyHostileUnits = hostiles;
	}
	protected RobotInfo[] getNearbyHostileUnits() {
		return ((Scout)executer).nearbyHostileUnits;
	}
	protected MapLocation getAverageEnemyStartingLocaction() {
		return ((Scout)executer).getAverageEnemyStartingLocaction();
	}
	protected RobotInfo getNearestHostileGardenerOrArchon() throws GameActionException {
		return ((Scout)executer).getNearestHostileGardenerOrArchon();
	}

}
