package sheep.util;


import battlecode.common.*;
import sheep.util.Geometry.Symmetry;

public class GameState {

	public static RobotController rc;

	// information about the map
	public static Symmetry symmetry;
	public static MapLocation centerOfMap, myInitialCenter, enemyInitialCenter;
	public static MapLocation[] myInitialLocations, enemyInitialLocations;
	public static TreeInfo[] nearbyNeutralTrees;

	// information about own team
	public static int archonCount, gardenerCount, soldierCount, lumberCount, scoutCount, tankCount;

	// information about self
	public static int spawnTurn;
	public static MapLocation spawnLocation;

}
