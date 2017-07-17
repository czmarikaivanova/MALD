import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;

public class Square {

	private Location center;
	private int diam;
	private ArrayList<Location> obstaclesFirst;
	private final int EXP_LIMIT;
	
	public Square(Location center) {
		this.center = center;
		this.diam = 0;
		obstaclesFirst = new ArrayList<Location>();
		EXP_LIMIT = 10;
	}
	
	/**
	 * expands the square around the central location
	 * @param map 
	 * @param forbidden set of locations that are not obstacles, but targets that has already been assigned
	 * @return the list of locations, that are supposed to be assigned as targets to available agents. It should be (almost) a bottleneck
	 */
	public LinkedList<Location> expand(Map map, ArrayList<Location> forbidden) {
		while (diam < EXP_LIMIT) {
			diam++;
			ArrayList<Location> fringeObsts = collectFringeObsts(map);
			Pair<ArrayList<Location>, ArrayList<Location>> cc = connected2Comps(fringeObsts, map);
			if (cc != null && cc.getSecond() != null) {  // there are two ccs
				Pair<Location, Location> closestPair = findClosestLoc(map, cc.getFirst(), cc.getSecond());
				return findPathGreedily(map, closestPair.getFirst(), closestPair.getSecond());
			}
			else {
				obstaclesFirst.addAll(fringeObsts);
			}
		}
		return null;
	}

	private Pair<ArrayList<Location>, ArrayList<Location>> connected2Comps(ArrayList<Location> fringeObsts, Map map) {
		ArrayList<Location> obstacles = new ArrayList<Location>();
		obstacles.addAll(fringeObsts);
		obstacles.addAll(obstaclesFirst);
		ArrayList<ArrayList<Location>> components = new ArrayList<ArrayList<Location>>();
		for (Location loc: obstacles) {
			ArrayList<Location> comp = new ArrayList<Location>();
			comp.add(loc);
			components.add(comp);
		}
		if (obstacles.isEmpty()) {
			return null;
		}
		// create componensts
		for (Location obst1: obstacles) {
			for (Location obst2: obstacles) {
				if (!obst1.equals(obst2)) {
					if (map.adjacent(obst1, obst2)) {
						ArrayList<Location> list1 = findMyList(components, obst1);
						ArrayList<Location> list2 = findMyList(components, obst2);
						if (!list1.equals(list2)) {
							list1.addAll(list2);
							components.remove(list2);
						}
					}
				}
			}
		}
		if (components.size() == 1) { // we have only one component
			return new Pair<ArrayList<Location>, ArrayList<Location>>(components.get(0), null);
		}
		else { //we have more components
			ArrayList<Location> first = components.get(0);
			components.remove(0);
			ArrayList<Location> rest = new ArrayList<Location>();
			for (ArrayList<Location> comp: components) {
				rest.addAll(comp);
			}
			return new Pair<ArrayList<Location>, ArrayList<Location>>(first, rest);
		}
	}

	private ArrayList<Location> findMyList(ArrayList<ArrayList<Location>> components, Location obst1) {
		for (ArrayList<Location> comp: components) {
			if (comp.contains(obst1)) {
				return comp;
			}
		}
		return null;
	}

	private ArrayList<Location> collectFringeObsts(Map map) {
		ArrayList<Location> fringe = new ArrayList<Location>();
		// top line
		int x = center.getX() - diam;
		int y;
		for (y = center.getY() - diam; y < center.getY() + diam; y++) {
			addObstToFringe(x, y, map, fringe);
		}
		// right line
		y = center.getY() + diam;
		for (x = center.getX() - diam; x < center.getX() + diam; x++) {
			addObstToFringe(x, y, map, fringe);
		}
		// bottom line
		x = center.getX() + diam;
		for (y = center.getY() + diam; y > center.getY() - diam; y--) {
			addObstToFringe(x, y, map, fringe);
		}
		// left line
		y = center.getY() - diam;
		for (x = center.getX() + diam; x > center.getX() - diam; x--) {
			addObstToFringe(x, y, map, fringe);
		}
		return fringe;
	}

	private void addObstToFringe(int x, int y, Map map, ArrayList<Location> fringe) {
		if (map.inMap(x, y)) {
			Location loc = map.getLocation(x, y);
			if (loc.isObstacle()) {
				fringe.add(loc);
			}
		}
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
	private Pair<Location, Location> findClosestLoc(Map map, ArrayList<Location> locList1, ArrayList<Location> locList2) {
		int minDst = Constants.INFINITY;
		Location minLoc1 = null;
		Location minLoc2 = null;
		for (Location l1: locList1) {
			for (Location l2: locList2) {
				int xl1 = l1.getX();
				int yl1 = l1.getY();
				int xl2 = l2.getX();
				int yl2 = l2.getY();
				int dst = Math.abs(xl1 - xl2) + Math.abs(yl1 - yl2);
				if (dst < minDst) {
					minDst = dst;
					minLoc1 = l1;
					minLoc2 = l2;
				}
			}
		}
		return new Pair<>(minLoc1, minLoc2);
	}
	
	/**
	 * check if current square contains a bottlenecks
	 * @param map
	 * @param forbidden
	 * @return
	 */
//	private boolean check(Map map, ArrayList<Location> forbidden) {
//		// top line
//		boolean ret = false;
//			int x = center.getX() - diam;
//			int y;
//			for (y = center.getY() - diam; y < center.getY() + diam; y++) {
//				if (isUsefulForBottleneck(x, y, map)) {
//					ret = true;
//				}
//			}
//			// right line
//			y = center.getY() + diam;
//			for (x = center.getX() - diam; x < center.getX() + diam; x++) {
//				if (isUsefulForBottleneck(x, y, map)) {
//					ret = true;
//				}
//			}
//			// bottom line
//			x = center.getX() + diam;
//			for (y = center.getY() + diam; y > center.getY() - diam; y--) {
//				if (isUsefulForBottleneck(x, y, map)) {
//					ret = true;
//				}
//			}
//			// left line
//			y = center.getY() - diam;
//			for (x = center.getX() + diam; x > center.getX() - diam; x--) {
//				if (isUsefulForBottleneck(x, y, map)) {
//					ret = true;
//				}
//			}
//		return ret;
//	}
	
	/**
	 * return true if a location is useful for a bottleneck. I. e., if
	 * it is not adjacent to any location in the soFarList
	 * @param x coordinate of the location
	 * @param y coordinate of the location
	 * @param map
	 * @return
	 */
//	private boolean isUsefulForBottleneck(int x, int y, Map map) {
//		if (map.inMap(x, y)) {
//			Location loc = map.getLocation(x, y);
//			if (loc.isObstacle() ) {
//				if (map.adjacent(loc, obstaclesFirst) && !obstaclesFirst.contains(loc) && !obstaclesSecond.contains(loc)|| obstaclesFirst.isEmpty() && !obstaclesSecond.contains(loc)) {
//					obstaclesFirst.add(loc);
//				}
//				else if (map.adjacent(loc, obstaclesSecond) && !obstaclesSecond.contains(loc) && !obstaclesFirst.contains(loc) || obstaclesSecond.isEmpty() && !obstaclesFirst.contains(loc)) {
//					obstaclesSecond.add(loc);
//					return true;
//				}
//			}
//		}
//		return false;
//	}
	
}
