package shepherd.Bot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shepherd.Bot.Behaviour.Behaviour;
import shepherd.Bot.Behaviour.Scout.InitialScoutBehaviour;

public class Scout extends Bot {

	public Scout(RobotController rc) {
		controller = rc;
	}

	public Behaviour getBehaviour() throws GameActionException {
		if(behaviour == null) return new InitialScoutBehaviour();
		if(behaviour instanceof InitialScoutBehaviour) return behaviour;
		return new InitialScoutBehaviour();
	}

}
