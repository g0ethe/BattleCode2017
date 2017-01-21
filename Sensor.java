package sheep.util;


import battlecode.common.Clock;
import battlecode.common.RobotController;

public class Sensor {

    public static RobotController rc;
    private static boolean[][] sensedIDs = new boolean[32][];


    /*
     * returns true if given id has already been sensed by this robot
     * (initializes stupidly large array portion-wise on the fly)
     */
    public static boolean hasSensedID(int id) {
    	int x = id-1;
    	if(sensedIDs[x/1000] == null) {
    		if(Clock.getBytecodesLeft() < 1100) Clock.yield();
    		sensedIDs[x/1000] = new boolean[1000];
    		return false;
    	}
    	else return sensedIDs[x/1000][x % 1000];
    }


    /*
     * adds given ID to the list of already sensed IDs
     * (initializes stupidly large array portion-wise on the fly)
     */
    public static void addSensedID(int id) {
    	int x = id - 1;
    	if(sensedIDs[x/1000] == null) {
    		if(Clock.getBytecodesLeft() < 1100) Clock.yield();
    		sensedIDs[x/1000] = new boolean[1000];
    		sensedIDs[x/1000][x % 1000] = true;
    	}
    	else sensedIDs[x/1000][x % 1000] = true;
    }

}
