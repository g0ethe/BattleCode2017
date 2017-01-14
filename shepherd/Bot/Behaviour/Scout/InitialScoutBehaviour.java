package shepherd.Bot.Behaviour.Scout;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class InitialScoutBehaviour extends ScoutBehaviour {

	RobotController scout;

	public void execute() throws GameActionException {
		if(scout == null) scout = executer.getController();
		Clock.yield();
	}

}
