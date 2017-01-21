package sheep.util;


import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Sensor {

    public static RobotController rc;
    private static int turnSensedNeutral = -1;
    private static boolean[][] sensedIDs = new boolean[32][];
    private static boolean[][] bulletTreeIDs = new boolean[32][];
    private static ArrayList<TreeInfo> neutralBulletTrees = new ArrayList<TreeInfo>(150);
    private static ArrayList<TreeInfo> neutralRobotTrees = new ArrayList<TreeInfo>(50);
    private static ArrayList<TreeInfo> neutralTrees = new ArrayList<TreeInfo>(500);

    /*
     * senses and returns nearby neutral trees as a list
     */
    public static ArrayList<TreeInfo> getNeutralTrees() {
    	senseNeutralTrees();
    	return neutralTrees;
    }


    /*
     * senses and returns nearby neutral trees with bullets as a list
     */
    public static ArrayList<TreeInfo> getNeutralBulletTrees() {
    	senseNeutralTrees();
    	return neutralBulletTrees;
    }


    /*
     * senses and returns nearby neutral trees with robots as a list
     */
    public static ArrayList<TreeInfo> getNeutralRobotTrees() {
    	senseNeutralTrees();
    	return neutralRobotTrees;
    }


    /*
     * senses neutral trees and saves them in list
     *
     * saves trees with bullets and robots on separate lists,
     * for faster look-up
     */
    private static void senseNeutralTrees() {
    	// avoid working on the same set of neutral trees twice
    	if(rc.getRoundNum() == turnSensedNeutral && !rc.hasMoved() &&!rc.hasAttacked()) return;
    	turnSensedNeutral = rc.getRoundNum();

    	TreeInfo[] trees = rc.senseNearbyTrees(rc.getType().sensorRadius, Team.NEUTRAL);
    	GameState.nearbyNeutralTrees = trees;
    	TreeInfo tree;
    	for(int i = trees.length; --i>=0;) {
    		tree = trees[i];
    		if(!hasSensedID(tree.getID())) {

    			// add new tree to sensed trees
    			addSensedID(tree.getID());
    			if(tree.containedBullets > 0) { neutralBulletTrees.add(tree); addBulletTreeID(tree.getID()); }
    			if(tree.containedRobot != null) neutralRobotTrees.add(tree);
    			neutralTrees.add(tree);

    		}
    		else {

    			// check for updating information on tree
    			if(tree.containedBullets <= 0) {
    				if(hasBullets(tree.getID())) {
    					removeBulletTreeID(tree.getID());
    					neutralBulletTrees.remove(tree.getID());
    				}
    			}

    		}
    	}
    }


    /*
     * returns true if given id has already been sensed by this robot
     * (initializes stupidly large array portion-wise on the fly)
     */
    private static boolean hasSensedID(int id) {
    	int x = id-1;
    	if(sensedIDs[x/1000] == null) {
    		if(Clock.getBytecodesLeft() < 1100) return false;
    		sensedIDs[x/1000] = new boolean[1000];
    		return false;
    	}
    	else return sensedIDs[x/1000][x % 1000];
    }


    /*
     * adds given ID to the list of already sensed IDs
     * (initializes stupidly large array portion-wise on the fly)
     */
    private static void addSensedID(int id) {
    	int x = id - 1;
    	if(sensedIDs[x/1000] == null) {
    		if(Clock.getBytecodesLeft() < 1100) Clock.yield();
    		sensedIDs[x/1000] = new boolean[1000];
    		sensedIDs[x/1000][x % 1000] = true;
    	}
    	else sensedIDs[x/1000][x % 1000] = true;
    }


    /*
     * adds a given id to the list of trees with bullets
     * (initializes stupidly large array portion-wise on the fly)
     */
    private static void addBulletTreeID(int treeID) {
    	int x = treeID - 1;
    	if(bulletTreeIDs[x/1000] == null) {
    		if(Clock.getBytecodesLeft() < 1100) Clock.yield();
    		bulletTreeIDs[x/1000] = new boolean[1000];
    		bulletTreeIDs[x/1000][x % 1000] = true;
    	}
    	else bulletTreeIDs[x/1000][x % 1000] = true;
    }


    /*
     * removes a given id from the list of trees with bullets
     * (initializes stupidly large array portion-wise on the fly)
     */
    private static void removeBulletTreeID(int treeID) {
    	int x = treeID - 1;
    	if(bulletTreeIDs[x/1000] == null) {
    		if(Clock.getBytecodesLeft() < 1100) Clock.yield();
    		bulletTreeIDs[x/1000] = new boolean[1000];
    	}
    	else bulletTreeIDs[x/1000][x % 1000] = false;
    }


    /*
     * removes a given id from the list of trees with bullets
     * (initializes stupidly large array portion-wise on the fly)
     */
    private static boolean hasBullets(int treeID) {
    	int x = treeID - 1;
    	if(bulletTreeIDs[x/1000] == null) {
    		if(Clock.getBytecodesLeft() < 1100) return false;
    		bulletTreeIDs[x/1000] = new boolean[1000];
    		return false;
    	}
    	else return bulletTreeIDs[x/1000][x % 1000];
    }





}
