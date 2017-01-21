package sheep;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import sheep.bot.Archon;
import sheep.bot.Bot;
import sheep.bot.Gardener;
import sheep.bot.Lumberjack;
import sheep.bot.Scout;
import sheep.bot.Soldier;
import sheep.bot.Tank;

public class RobotPlayer {

	public static void run(RobotController rc) throws GameActionException {
		Bot.initialize(rc);
		switch(rc.getType()) {
		case ARCHON:		Archon.run(rc);			break;
		case GARDENER:		Gardener.run(rc);		break;
		case LUMBERJACK:	Lumberjack.run(rc);		break;
		case SCOUT:			Scout.run(rc);			break;
		case SOLDIER:		Soldier.run(rc);		break;
		case TANK:			Tank.run(rc);			break;
		}
	}

}
