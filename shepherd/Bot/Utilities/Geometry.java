package shepherd.Bot.Utilities;

public class Geometry {

	/* given a line, by two points L1 = (L1x, L1y) and L2 = (L2x, L2y) and a circle by center C = (Cx, Cy) and radius r,
	 * calculates if the line intersects with the circle */
	public static boolean lineIntersectsCircle(float L1x, float L1y, float L2x, float L2y, float Cx, float Cy, float r) {
		if(L1x != L2x && L1y != L2y) {
			float m = (L2y - L1y) / (L2x - L1x);
			float b = -1*m * L2x + L2y;
			float x = (m*m + 1);
			float y = 2*(m*b - m*Cy - Cx);
			float z = Cy*Cy - r*r + Cx*Cx - 2*b*Cy + b*b;
			return y*y - 4*x*z >= 0;
		}
		else if(L1x == L2x && L1y != L2y) {
			float dx = (Cx - L1x < 0) ? (L1x - Cx) : (Cx - L1x);
			return dx > r;
		}
		else if(L1x != L2x && L1y == L2y) {
			float dy = (Cy - L1y < 0) ? (L1y - Cy) : (Cy - L1y);
			return dy > r;
		}
		else return (L1x - Cx)*(L1x - Cx) + (L1y - Cy)*(L1y - Cy) <= r*r;
	}

}
