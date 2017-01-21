package sheep.bot;


import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import sheep.util.Broadcast;
import sheep.util.GameState;
import sheep.util.Sensor;



public class Archon extends Bot {

	public static void run(RobotController rc) throws GameActionException {
		while(true) {
			Broadcast.updateUnitCount();
			debug_sensor();
			Clock.yield();
		}
	}

	public static void debug_sensor() throws GameActionException {
		headlessChicken();
		System.out.println("# neutral trees: " + Sensor.getNeutralTrees().size());
		System.out.println("# bullet trees: " + Sensor.getNeutralBulletTrees().size());
		System.out.println("# robot trees: " + Sensor.getNeutralRobotTrees().size());
		System.out.println("# archons: " + GameState.archonCount);
		System.out.println("# gardeners: " + GameState.gardenerCount);
		Direction dir = Direction.getEast();
		for(int i = 0; i < 360; i++) {
			if(bot.canHireGardener(dir.rotateLeftDegrees(i))) { bot.hireGardener(dir.rotateLeftDegrees(i)); break; }
		}
	}

}
