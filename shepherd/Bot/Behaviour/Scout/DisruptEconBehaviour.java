package shepherd.Bot.Behaviour.Scout;


import battlecode.common.Clock;
import battlecode.common.GameActionException;



public class DisruptEconBehaviour extends ScoutBehaviour {

	public void execute() throws GameActionException {
		initialize();
		Clock.yield();
	}

}
