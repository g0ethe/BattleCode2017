package shepherd.Bot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shepherd.Bot.Behaviour.Behaviour;
import shepherd.Bot.Utilities.NavigationSystem;
import shepherd.Bot.Utilities.Radar;
import shepherd.Bot.Utilities.Radio;

public abstract class Bot {

	public abstract Behaviour getBehaviour() throws GameActionException;
	public static RobotController controller;
	public static Behaviour behaviour;
	public static Radar radar;
	public static Radio radio;
	public static NavigationSystem navi;

	public RobotController getController() { return controller; }

	public void run() throws GameActionException {
		while(true) {
			behaviour = getBehaviour();
			behaviour.executer = this;
			behaviour.execute();
		}
	}

}
