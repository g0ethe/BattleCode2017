package sheep.bot;



import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import sheep.util.Broadcast;
import sheep.util.GameState;
import sheep.util.Geometry;
import sheep.util.Sensor;

public abstract class Bot {

    public static RobotController bot;



    /*
     * initializes helper classes and calculates some information on the map
     */
    public static void initialize(RobotController rc) throws GameActionException {
        Broadcast.rc = rc;
        GameState.rc = rc;
        Sensor.rc = rc;
        Geometry.rc = rc;
        GameState.symmetry = Geometry.getSymmetry();
        GameState.myInitialLocations = rc.getInitialArchonLocations(rc.getTeam());
        GameState.enemyInitialLocations = rc.getInitialArchonLocations(rc.getTeam().opponent());
        GameState.myInitialCenter = Geometry.centerOf(GameState.myInitialLocations);
        GameState.enemyInitialCenter = Geometry.centerOf(GameState.enemyInitialLocations);
        GameState.centerOfMap = Geometry.centerOf(GameState.enemyInitialCenter, GameState.myInitialCenter);
        GameState.archonCount = GameState.myInitialLocations.length;
        GameState.gardenerCount = 0;
        GameState.soldierCount = 0;
        GameState.lumberCount = 0;
        GameState.scoutCount = 0;
        GameState.tankCount = 0;
        GameState.spawnLocation = rc.getLocation();
        GameState.spawnTurn = rc.getRoundNum();
        bot = rc;
    }

    public static void headlessChicken() throws GameActionException {
		float random = (float)(Math.random()*360);
		if(bot.canMove(Direction.getEast().rotateLeftDegrees(random))) bot.move(Direction.getEast().rotateLeftDegrees(random));
	}

}
