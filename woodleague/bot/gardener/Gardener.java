package woodleague.bot.gardener;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.TreeInfo;
import woodleague.bot.Bot;
import woodleague.util.Broadcast;
import woodleague.util.GameState;


public class Gardener extends Bot {

    public enum Buildable {
        TREE, LUMBERJACK, SCOUT, SOLDIER, TANK
    }

    public static void run() throws GameActionException {
        while(true) {
            long turn = rc.getRoundNum();

            Broadcast.updateUnitCount();
            MapLocation opening = farm();
            if(opening != null) rc.setIndicatorDot(opening, 255, 255, 255);

            if(turn == rc.getRoundNum()) Clock.yield();
        }
    }

    /*
     * runs in a circle and plants trees, watering them
     * returns opening location, once all trees are built
     * returns null if flower not yet complete
     * TODO: find a better flowerCenter (away from map edges)
     */
    private static MapLocation[] petalLocations;
    private static float petalPlantDistance;
    private static MapLocation farm() throws GameActionException {

        // calculate flower:
        // alpha: 		 angle for circular movement inside the flower
        // beta: 		 angle between to neighboring trees (measured from flower center)
        // flowerRadius: distance from center point, on which the centers of trees will be built
        // innerRadius:  the radius of the circle, a gardener can walk in 10 turns
        float turnsToWater = GameConstants.WATER_HEALTH_REGEN_RATE / GameConstants.BULLET_TREE_DECAY_RATE;
        float innerPerimeter = rc.getType().strideRadius * turnsToWater;
        float innerRadius = (float) (innerPerimeter / (2*Math.PI));
        float outerRadius = innerRadius + rc.getType().bodyRadius;
        float flowerRadius = outerRadius + GameConstants.BULLET_TREE_RADIUS;
        float flowerPerimeter = (float) (2*Math.PI*flowerRadius);
        float stride = rc.getType().strideRadius;
        float alpha = (float) Math.abs(Math.acos( (stride * stride) / (2*stride*innerRadius)));
        float beta = (float) Math.abs(Math.acos( 1 - (2*GameConstants.BULLET_TREE_RADIUS*GameConstants.BULLET_TREE_RADIUS)/(flowerRadius*flowerRadius) ));
        int treesPerFlower = (int) (flowerPerimeter / (2*GameConstants.BULLET_TREE_RADIUS)) - 1;
        Direction flowerDir = GameState.mySpawn.directionTo(GameState.enemyCenter).opposite();
        MapLocation flowerCenter = GameState.mySpawn.add(flowerDir, innerRadius);
        Direction circular = (rc.getLocation().directionTo(flowerCenter)).rotateRightRads(alpha);

        // initial building: build a tree in any direction, flowerRadius away from flowerCenter
        TreeInfo[] trees = rc.senseNearbyTrees(flowerCenter, flowerRadius, rc.getTeam());
        if(trees.length == 0) {
            // assuming we defined the flowerCenter in a way that we are now guaranteed to be
            // on the inner circle of the future flower
            Direction awayFromCenter = flowerCenter.directionTo(rc.getLocation());
            if(rc.canPlantTree(awayFromCenter)) { rc.plantTree(awayFromCenter); }

            // get the tree we just planted and outline the location of other trees in this flower
            trees = rc.senseNearbyTrees(flowerCenter, flowerRadius, rc.getTeam());
            if(trees.length > 0) {
                Direction dir = flowerCenter.directionTo(trees[0].getLocation());
                float dist = flowerCenter.distanceTo(trees[0].getLocation());
                petalLocations = new MapLocation[treesPerFlower];
                petalLocations[0] = trees[0].getLocation();
                petalPlantDistance = rc.getLocation().distanceTo(flowerCenter);
                for(int i = 1; i < treesPerFlower; i++) {
                    petalLocations[i] = flowerCenter.add(dir.rotateLeftRads(beta * i), dist);
                    rc.setIndicatorDot( petalLocations[i], 255-i, 0, 0);
                }
            }
        }

        // move to other petal Planting Locations and plant trees from there, away from center,
        // until 7 trees have been planted
        if(trees.length < treesPerFlower && rc.isBuildReady() && rc.hasTreeBuildRequirements()) {

        	// find nearest, empty plant location
        	MapLocation nearest = null; float nearestDist = Float.MAX_VALUE;
            for(int i = petalLocations.length; --i>=0;) {
                MapLocation curr = petalLocations[i];
                if(rc.isCircleOccupiedExceptByThisRobot(curr, GameConstants.BULLET_TREE_RADIUS)) {
                	rc.setIndicatorDot(curr, 255-i, 0, 0);
                	continue;
                }
                rc.setIndicatorDot(curr, 0, 255-i, 0);
                float d = rc.getLocation().distanceTo(curr);
                if(d < nearestDist) {
                	nearestDist = d;
                	nearest = curr;
                }
            }

            // move towards a location from which you can plant at the given nearest empty location
            if(nearest != null) {
            	rc.setIndicatorDot(nearest, 0, 0, 255);
            	Direction dir = flowerCenter.directionTo(nearest);
            	float dist = petalPlantDistance;
            	MapLocation plantingLocation = flowerCenter.add(dir, dist);
            	float distBefore = rc.getLocation().distanceTo(nearest), distAfter = 0;
            	if(!rc.hasMoved() && rc.canMove(plantingLocation)) {
            		rc.move(plantingLocation);
            		distAfter = rc.getLocation().distanceTo(nearest);
            	}
            	if(rc.getLocation().distanceTo(nearest) <= GameConstants.GENERAL_SPAWN_OFFSET + rc.getType().bodyRadius + GameConstants.BULLET_TREE_RADIUS) {
            		if(rc.canPlantTree(rc.getLocation().directionTo(nearest)))
            			rc.plantTree(rc.getLocation().directionTo(nearest));
            	}

            	// work around the stupid api and floating point shit
            	if(rc.hasMoved() && distBefore == distAfter && rc.isBuildReady() && rc.hasTreeBuildRequirements()) {
            		if(rc.canPlantTree(rc.getLocation().directionTo(nearest)))
            			rc.plantTree(rc.getLocation().directionTo(nearest));
            		if(rc.canPlantTree(rc.getLocation().directionTo(nearest).rotateLeftRads(0.01f)))
            			rc.plantTree(rc.getLocation().directionTo(nearest).rotateLeftRads(0.01f));
            		if(rc.canPlantTree(rc.getLocation().directionTo(nearest).rotateRightRads(0.01f)))
            			rc.plantTree(rc.getLocation().directionTo(nearest).rotateRightRads(0.01f));
            	}
            }
        }

        // circular movement inside flower to water stuff
        else if(!rc.hasMoved() && rc.canMove(circular)) rc.move(circular);
        trees = rc.senseNearbyTrees(flowerCenter, flowerRadius, rc.getTeam());
        for(TreeInfo tree : trees) {
        	if(tree.getHealth() <= tree.getMaxHealth() - GameConstants.WATER_HEALTH_REGEN_RATE/2 && rc.canWater(tree.getID()))
        		rc.water(tree.getID());
        }



        // return opening location of flower, or null if flower is not yet complete
        MapLocation first = petalLocations[0], last = petalLocations[petalLocations.length-1];
        MapLocation middle = new MapLocation((first.x + last.x)/2 , (first.y + last.y)/2);
        float dist = flowerCenter.distanceTo(first);
        Direction dir = flowerCenter.directionTo(middle);
        return flowerCenter.add(dir, dist);
    }




}
