package woodleague;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import woodleague.bot.Bot;
import woodleague.bot.archon.Archon;
import woodleague.bot.gardener.Gardener;
import woodleague.bot.lumberjack.Lumberjack;
import woodleague.bot.scout.Scout;

/*
 * Aim is to attack in waves:
 * First wave with some scouts:
 * 		scouts move towards enemy spawn locations, gathering bullets on the way
 * 		once there, find and harass enemy gardeners in packs of three
 * 		until the next wave starts.
 * Second wave with rest of scouts and lumberjacks:
 * 		scouts now relay information to archons for distributed pathfinding calculations
 * 		lumberjacks chop neutral trees for robots and to clear the path for the next wave
 * 		once at the enemy, attack whatever is found, but try to deal as much economical damage as possible
 * Last wave with tanks and maybe some scouts:
 * 		scouts relay information about good targets
 * 		tanks shoot, trying to deal as much damage as possible
 */

public class RobotPlayer {

	public static void run(RobotController rc) throws GameActionException {
		Bot.initialize(rc);
		switch(rc.getType()) {
		case ARCHON: Archon.run(); break;

		case GARDENER: Gardener.run(); break;

		case LUMBERJACK: Lumberjack.run(); break;

		case SCOUT: Scout.run(); break;

		case SOLDIER:
			break;
		case TANK:
			break;
		}
	}

}
