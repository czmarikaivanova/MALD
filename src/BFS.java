import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class BFS extends Algorithm {

	private HashMap<Location, Boolean> flags = new HashMap<Location, Boolean>(); // already visited
	private HashMap<Location, Location> prevs = new HashMap<Location, Location>();
	private int considerAgents;
	
	
	public BFS(int considerAgents) {
		super();
		this.considerAgents = considerAgents;
	}
	
	@Override
	public LinkedList<Location> findPath(Location start, Location target, Map map) {
		LinkedList<Location> path = new LinkedList<Location>();	
		if (target != null) { // target null means we want all paths from start
			path.add(target);
			if (start.equals(target)) {
				return path; // don't move if you have arrived there
			}
		}
		flags = new HashMap<Location, Boolean>();
		prevs = new HashMap<Location, Location>();
		for (Location loc: map) { // INIT
			flags.put(loc, false);
			prevs.put(loc, null);
		}
		LinkedList<Location> queue = new LinkedList<Location>();
		queue.add(start);
		flags.put(start, true);
		while (!queue.isEmpty()) { // BFS SEARCH
			Location loc = queue.remove();
			if (loc == null) {
				System.out.println("Removed a null location from the queue");
			}
			ArrayList<Location> neighbours = map.neighbors(loc, false);
			for (Location adjLoc: neighbours) {
				if (adjLoc == null) {
					System.out.println("adjLoc = null");
				}
				if (!adjLoc.isObstacle() && flags.get(adjLoc).equals(Boolean.FALSE)  ) { // it will not be null
					if (adjLoc.getAgent() == null || considerAgents == Constants.CONSIDER_AGENTS_NONE || (considerAgents == Constants.CONSIDER_AGENTS_OPPONENT) && start.getAgent().getTeam() == adjLoc.getAgent().getTeam()) {
							flags.put(adjLoc, true);
							prevs.put(adjLoc, loc);
							
							queue.add(adjLoc);
					}
				}
			}
		}
		if (target != null) {
			Location loc = target;			// retrieve the next location
			Location prevLoc = prevs.get(loc); // has to exist, because the case where the agent has reached the target is treated at the beginning
			if (prevLoc == null) { // no path found
				return new LinkedList<Location>(); // should be empty
			}
			while (!prevLoc.equals(start)) {
				loc = prevLoc;
				path.add(0, loc); // add next location to the beginning
				prevLoc = prevs.get(loc);
			}
			return path;
		}
		else {
			return null; // if target is null, we don't want any path. We need to fill the data structures only.
		}
	}
	
	public LinkedList<Location> findPathWithout(Location start, Location target, ArrayList<Location> forbidden, Map map) {
		LinkedList<Location> path = new LinkedList<Location>();	
		if (target != null) { // target null means we want all paths from start
			path.add(target);
			if (start.equals(target)) {
				return path; // don't move if you have arrived there
			}
		}
		flags = new HashMap<Location, Boolean>();
		prevs = new HashMap<Location, Location>();
		for (Location loc: map) { // INIT
			flags.put(loc, false);
			prevs.put(loc, null);
		}
		LinkedList<Location> queue = new LinkedList<Location>();
		queue.add(start);
		flags.put(start, true);
		while (!queue.isEmpty()) { // BFS SEARCH
			Location loc = queue.remove();
			if (loc == null) {
				System.out.println("Removed a null location from the queue");
			}
			ArrayList<Location> neighbours = map.neighbors(loc, false);
			for (Location adjLoc: neighbours) {
				if (adjLoc == null) {
					System.out.println("adjLoc = null");
				}
				if (forbidden.contains(adjLoc)) {
					System.out.println("Forbidden contains adjLoc");
				}
				if (!adjLoc.isObstacle() && flags.get(adjLoc).equals(Boolean.FALSE) && !forbidden.contains(adjLoc))  { // it will not be null
					if (adjLoc.getAgent() == null || considerAgents == Constants.CONSIDER_AGENTS_NONE || (considerAgents == Constants.CONSIDER_AGENTS_OPPONENT) && start.getAgent().getTeam() == adjLoc.getAgent().getTeam()) {
							flags.put(adjLoc, true);
							prevs.put(adjLoc, loc);
							
							queue.add(adjLoc);
					}
				}
			}
		}
		if (target != null) {
			Location loc = target;			// retrieve the next location
			Location prevLoc = prevs.get(loc); // has to exist, because the case where the agent has reached the target is treated at the beginning
			if (prevLoc == null) { // no path found
				return new LinkedList<Location>(); // should be empty
			}
			while (!prevLoc.equals(start)) {
				loc = prevLoc;
				path.add(0, loc); // add next location to the beginning
				prevLoc = prevs.get(loc);
			}
			return path;
		}
		else {
			return null; // if target is null, we don't want any path. We need to fill the data structures only.
		}
	}
	
	
	public int minPathLength(Location start, Location target, Map map) {
		return findPath(start, target, map).size();
	}

	/**
	 * get distances from a particular node to all the remaining nodes.
	 * @param map
	 * @param start node for which we want to calculate the distances.
	 * @return
	 */
	public int[] distsToLocation(Map map, Location start) {
		findPath(start, null, map);
		int[] dists = new int[map.getLocationCount()];
		for (Location loc : map) {
			if (!loc.isObstacle()) {
				int dst = 0;
				Location node = loc;
				while (!node.equals(start)) {
					node = prevs.get(node);
					if (node == null) { // no path exist (blocked)
						dst = Constants.INFINITY;
						break;
					}
					dst++;
				}
				dists[loc.getId()] = dst;
			}
		}
		return dists;
	}

	
}

