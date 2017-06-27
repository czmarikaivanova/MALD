import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class BFS extends Algorithm {

	@Override
	public LinkedList<Location> findPath(Location start, Location target, Map map) {
		LinkedList<Location> path = new LinkedList<Location>();	
		path.add(target);
		if (start.equals(target)) {
			return path; // don't move if you have arrived there
		}
		HashMap<Location, Boolean> flags = new HashMap<Location, Boolean>();
		HashMap<Location, Location> prevs = new HashMap<Location, Location>();
		for (Location loc: map) { // INIT
			flags.put(loc, false);
			prevs.put(loc, null);
		}
		LinkedList<Location> queue = new LinkedList<Location>();
		queue.add(start);
		flags.put(start, true);
		while (!queue.isEmpty()) { // BFS SEARCH
			Location loc = queue.remove();
			for (int dir1 = -1; dir1 <= 1; dir1++) {
				for (int dir2 = -1; dir2 <= 1; dir2++) {
					if (Math.abs(dir1) != Math.abs(dir2)) { // do not include the current node in BFS and do not consider diagonals
						int x = loc.getX() + dir1;
						int y = loc.getY() + dir2;
						if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight()) { // we should not get out of the map
							Location adjLoc = map.getLocation(x, y);
							if (!adjLoc.isObstacle() && flags.get(adjLoc).equals(Boolean.FALSE)) { // it will not be null
								flags.put(adjLoc, true);
								prevs.put(adjLoc, loc);
								queue.add(adjLoc);
							}
						}
					}
				}
			}
		}
		Location loc = target;			// retrieve the next location
		Location prevLoc = prevs.get(loc); // has to exist, because the case where the agent has reached the target is treated at the beginning
		while (!prevLoc.equals(start)) {
			loc = prevLoc;
			path.add(0, loc); // add next location to the beginning
			prevLoc = prevs.get(loc);
		}
		return path;
	}

}

