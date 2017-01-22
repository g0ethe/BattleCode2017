    /*
     * returns if given body is in specified rectangle
     *
     * @ param:
     * 		BodyInfo body: the body to be checked against the rectangle
     * 		Direction dir: the direction the rectangle is aligned with
     * 		MapLocation M: middle point of outer-most edge of rectangle
     * 		float h: half the rectangles height
     */
    public static boolean isInRectangle(BodyInfo body, Direction dir, MapLocation M, float h) {
    	 float alpha = Math.abs(M.directionTo(body.getLocation()).radiansBetween(dir));
    	 if(alpha >= Math.PI/2) return false;
    	 float d = M.distanceTo(body.getLocation());
    	 float x = (float)(d * Math.sin(alpha));
    	 return x - h <= body.getRadius();
    }