import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class Square {

	private Location center;
	private int diam;
	private ArrayList<Location> obstSoFar;
	private final int EXP_LIMIT;
	
	public Square(Location center) {
		this.center = center;
		this.diam = 0;
		obstSoFar = new ArrayList<Location>();
		EXP_LIMIT = 5;
	}
	
	/**
	 * expands the square around the central location
	 * @param map 
	 * @param forbidden set of locations that are not obstacles, but targets that has already been assigned
	 * @return the list of locations, that are supposed to be assigned as targets to available agents. It should be (almost) a bottleneck
	 */
	public LinkedList<Location> expand(Map map, ArrayList<Location> forbidden) {
		boolean bnFound = false;
		while (!bnFound && diam < EXP_LIMIT) {
			diam++;
			Location nextObst = check(map, forbidden);
			if (nextObst != null) {
				bnFound = true;
				// TODO: consider including the forbidden list as stated in the following two lines
//				ArrayList<Location> obstAndForbiddenList = new ArrayList<Location>(obstSoFar);
//				obstAndForbiddenList.addAll(forbidden);
				Location closestLocToNext = findClosestLoc(map, nextObst, obstSoFar);
				return findPathGreedily(map, nextObst, closestLocToNext);
//				return new BFS(Constants.CONSIDER_AGENTS_NONE).findPath(nextObst, closestLocToNext, map);  // possibly better
			}
		}
		return null;
	}
	
	private LinkedList<Location> findPathGreedily(Map map, Location loc1, Location loc2) {
		LinkedList<Location> path = new LinkedList<Location>();
		int x = loc1.getX();
		int y = loc1.getY();
		while (x != loc2.getX() || y != loc2.getY()) {
			int currDst = Math.abs(x - loc2.getX()) + Math.abs(y - loc2.getY());
			int dir1 = 0;
			int dir2 = 0;
			Location nextLoc;
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					int dst = Math.abs(x + i - loc2.getX()) + Math.abs(y + j - loc2.getY());
					if (dst < currDst) {
						dir1 = i;
						dir2 = j;
					}
				}
			}
			x = x + dir1;
			y = y + dir2;
			nextLoc = map.getLocation(x, y);
			path.add(nextLoc);
		}
		path.removeLast(); // it is an obstacle
		return path;
	}

	//bfs not necessary, we will exploit the grid graph properties
	private Location findClosestLoc(Map map, Location nextObst, ArrayList<Location> locList) {
		int minDst = Constants.INFINITY;
		Location closest = null;
		int x = nextObst.getX();
		int y = nextObst.getY();
		for (Location loc: locList) {
			int xl = loc.getX();
			int yl = loc.getY();
			int dst = Math.abs(x - xl) + Math.abs(y - yl);
			if (dst < minDst) {
				minDst = dst;
				closest = loc;
			}
		}
		return closest;
	}
	
	/**
	 * check if current square contains a bottlenecks
	 * @param map
	 * @param forbidden
	 * @return
	 */
	private Location check(Map map, ArrayList<Location> forbidden) {
		// top line
		int x = center.getX() - diam;
		int y;
		for (y = center.getY() - diam; y < center.getY() + diam; y++) {
			if (isUsefulForBottleneck(x, y, map)) {
				return map.getLocation(x, y);
			}
		}
		// right line
		y = center.getY() + diam;
		for (x = center.getX() - diam; x < center.getX() + diam; x++) {
			if (isUsefulForBottleneck(x, y, map)) {
				return map.getLocation(x, y);
			}
		}
		// bottom line
		x = center.getX() + diam;
		for (y = center.getY() + diam; y > center.getY() - diam; y--) {
			if (isUsefulForBottleneck(x, y, map)) {
				return map.getLocation(x, y);
			}
		}
		// left line
		y = center.getY() - diam;
		for (x = center.getX() + diam; x > center.getX() - diam; x--) {
			if (isUsefulForBottleneck(x, y, map)) {
				return map.getLocation(x, y);
			}
		}
		return null;
	}
	
	/**
	 * return true if a location is useful for a bottleneck. I. e., if
	 * it is not adjacent to any location in the soFarList
	 * @param x coordinate of the location
	 * @param y coordinate of the location
	 * @param map
	 * @return
	 */
	private boolean isUsefulForBottleneck(int x, int y, Map map) {
		Location loc = map.getLocation(x, y);
		if (loc.isObstacle() ) {
			if (map.adjacent(loc, obstSoFar) || obstSoFar.isEmpty()) {
				obstSoFar.add(loc);
			}
			else {
				return true;
			}
		}
		return false;
	}
	
}
