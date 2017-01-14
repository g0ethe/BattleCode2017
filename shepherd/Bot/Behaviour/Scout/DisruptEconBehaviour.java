package shepherd.Bot.Behaviour.Scout;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import shepherd.Bot.Scout;

public class DisruptEconBehaviour extends ScoutBehaviour {

	ArrayList<MapLocation> neutralTreeLocations;
	RobotController scout;



	public void execute() throws GameActionException {
		initialize();
		Clock.yield();
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
