
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class BottleneckStrategy extends Strategy {

	ArrayList<ArrayList<Location>> bottlenecks;
	Team offTeam;
	Team defTeam;
	Map map;
	
	public BottleneckStrategy() {
		super();
		this.bottlenecks = null;
	}

	/**
	 * Assign targets to defending agents according to frequently used bottlenecks found in the map
	 */
	@Override
	public void allocateTargets(Map map, Team defTeam, Team offTeam,  boolean reallocate, int considerAgents) {
		this.offTeam = offTeam;
		this.defTeam = defTeam;
		this.map = map;
		if (bottlenecks == null) { // first call of this method
			bottlenecks = findBottlenecks(3);
		}
		ArrayList<Agent> agentsToAllocate;
		if (reallocate) { // if we want to reallocate, we will add all the agents to the list, including those who are at their targsts
			agentsToAllocate = new ArrayList<Agent>();
			for (Agent a: defTeam) {
				agentsToAllocate.add(a);
			}
		}
		else { // if we don't want to reallocate, we will add only agents that haven't reached their targets.
			agentsToAllocate = new ArrayList<Agent>();
			for (Agent a: defTeam) {
				if (!a.atTarget()) {
					agentsToAllocate.add(a);
				}
			}
		}
		ArrayList<ArrayList<Location>> paths = estimatePaths(considerAgents, map);
		HashMap<ArrayList<Location>, Integer> bottleneckPassFreqs = new HashMap<ArrayList<Location>, Integer>(); 
		for (ArrayList<Location> bneck: bottlenecks) {
			bottleneckPassFreqs.put(bneck, 0);
			for (ArrayList<Location> path : paths) {
//				ArrayList<Location> intersect = (ArrayList<Location>) bneck.stream().filter(path::contains).collect(Collectors.toList()); // find an intersection of two lists
				if (hasCommonElements(bneck, path)) {
					bottleneckPassFreqs.replace(bneck, bottleneckPassFreqs.get(bneck) + 1);
				}
			}
		}
		ArrayList<Location> bottleneck;
		while((bottleneck = getBottleNeckOfFreq(bottleneckPassFreqs, 10)) != null) {
			if (agentsToAllocate.size() < bottleneck.size()) {
				break; // while
			}
			assignBottleneck(bottleneck, agentsToAllocate, map); // items in agentsToAllocate are removed inside this call
			bottleneckPassFreqs.remove(bottleneck);
		}
		if (!agentsToAllocate.isEmpty()) {
			new RandomStrategy().allocateTargets(map, defTeam, offTeam, reallocate, considerAgents);
//			allocateTargetsRandom();
		}
		for (ArrayList<Location> bneck: bottlenecks) {
			System.out.println(bneck.toString() + " paths : " + bottleneckPassFreqs.get(bneck));
		}
	}
	
	/**
	 * Assign locations of a specified bottleneck to appropriate agents that still haven't been allocated
	 * @param bottleneck - locations to be assinged
	 * @param agentsToAllocate agents that still haven't been allocated and can be used
	 */
	private void assignBottleneck(ArrayList<Location> bottleneck, ArrayList<Agent> agentsToAllocate, Map map) {
		for(Location b: bottleneck) {
			Collections.sort(agentsToAllocate, new DistToLocationComparator(map, b)); // can be faster by placing in front of cycle, with a minor loss of accuracy
			Agent a = agentsToAllocate.remove(0);
			a.setTargetLocation(b);
		}
	}

	/**
	 * Get a bottleneck
	 * @param bottleneckPassFreqs
	 * @param f
	 * @return
	 */
	private ArrayList<Location> getBottleNeckOfFreq(HashMap<ArrayList<Location>, Integer> bottleneckPassFreqs, int f) {
		Iterator it = bottleneckPassFreqs.entrySet().iterator();
		while(it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry) it.next();
			int realF = (int) pair.getValue();
			if (realF > f) {
				return (ArrayList<Location>) pair.getKey();
			}
		}
		return null;
	}

	/**
	 * randomly assign targets to offensive agents and calculate shortest paths
	 * @return
	 */
	private ArrayList<ArrayList<Location>> estimatePaths(int considerAgents, Map map) {
		ArrayList<ArrayList<Location>> paths = new ArrayList<ArrayList<Location>>();
		ArrayList<Location> targets = map.getTargets();
		int i = 0;
		for (Agent a : offTeam) {
			Location t = targets.get(i);  // guess a target by id
			i++;
			LinkedList<Location> path = new BFS(considerAgents).findPath(a.getCurrentLocation(), t, map); 
			paths.add(new ArrayList<Location>(path));
		}
		return paths;
	}
	
	private boolean hasCommonElements(ArrayList<Location> list1, ArrayList<Location> list2) {
		for (Location t1: list1) {
			for (Location t2: list2) {
				if (t1.equals(t2)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * find bottleneck of a specified width
	 * @param width - specified width
	 * @return - list of bottlenecks of the specified width
	 */
	private ArrayList<ArrayList<Location>> findBottlenecks(int width) {
		ArrayList<ArrayList<Location>> bottleneckList = new ArrayList<ArrayList<Location>>();
		// check rows
		for (int i = 0; i < map.getHeight(); i++) {
			for (int j = 1; j < map.getWidth()-width; j++) {
				if(isEmptyWinRow(i, j, width) && map.getLocation(i, j - 1).isObstacle() && map.getLocation(i, j + width).isObstacle()) { // check for an empty sequennce of locations and whether it is surrounded by obstacles
					ArrayList<Location> bneck = new ArrayList<Location>();
					for (int k = 0; k < width; k++) {
						bneck.add(map.getLocation(i, j + k));
					}
					bottleneckList.add(bneck);
				}
			}
		}
		// check columns
		for (int j = 0; j < map.getWidth(); j++) {
			for (int i = 1; i < map.getHeight()-width; i++) {

				if(isEmptyWinCol(j, i, width) && map.getLocation(i - 1, j).isObstacle() && map.getLocation(i + width, j).isObstacle()) { // check for an empty sequennce of locations and whether it is surrounded by obstacles
					ArrayList<Location> bneck = new ArrayList<Location>();
					for (int k = 0; k < width; k++) {
						bneck.add(map.getLocation(i + k, j));
					}
					bottleneckList.add(bneck);
				}
			}
		}
		return bottleneckList;
	}


	private boolean isEmptyWinRow(int row, int j, int size) {
		for (int k = 0; k < size; k++) {
			if (map.getLocation(row, j + k).isObstacle()) {
				return false;
			}
		}
		return true;
	}

	private boolean isEmptyWinCol(int col, int j, int size) {
		for (int k = 0; k < size; k++) {
			if (map.getLocation(j + k, col).isObstacle()) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Compares two agents accordint to their distance to a specified location
	 * @author marika
	 *
	 */
	private class DistToLocationComparator implements Comparator<Agent> {
		BFS bfs;
		Map map;
		Location loc;
		
		public DistToLocationComparator(Map map, Location loc) {
			super();
			this.map = map;
			this.loc = loc;
			bfs = new BFS(Constants.CONSIDER_AGENTS_NONE);
		}

		@Override
		public int compare(Agent o1, Agent o2) {
			int d1 = bfs.minPathLength(o1.getCurrentLocation(), loc, map);
			int d2 = bfs.minPathLength(o2.getCurrentLocation(), loc, map);
			return Integer.compare(d1, d2);
		}
		
	}
	


}
