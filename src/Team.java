import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

public class Team implements Iterable<Agent> {
	private ArrayList<Agent> agents;
	private ArrayList<Location> targets;
	private int id;

	private Map map;
	private Team otherTeam;
	
	/**
	 * constructor
	 * @param id
	 */
	public Team(int id, Map map, Team otherTeam) {
		this.id = id;
		agents = new ArrayList<Agent>();
		
		this.map = map;
		this.otherTeam = otherTeam;
		this.targets = map.getTargets();
	}
	
	/**
	 * insert an agent. It will happen only at the beginning
	 * @param a
	 */
	public void add(Agent a) {
		a.setAlg(new LRAstar(true));
		agents.add(a);
	}
	
	/**
	 * return an agent with a desired id
	 * @return
	 */
	public Agent getAgentById(int agentId) {
		for (Agent a: agents) {
			if (a.getId() == agentId) {
				return a;
			}
		}
		System.err.println("Agent with id " + id + "does not exist!");
		return null;
	}
	
	public String toString() {
		return (id == Constants.OFFENSIVE_TEAM ? Constants.OFFENSIVE_TEAM_NAME : Constants.DEFENSIVE_TEAM_NAME) + agents.toString();
	}

	/**
	 * returns the iterator over the list of agents
	 */
	public Iterator<Agent> iterator() {
		return new AgentIterator();
	}
	
	/**
	 * private class implementing iterator over the list of agents
	 * @author marika
	 *
	 */
	private class AgentIterator implements Iterator<Agent> {
		private int cursor;

		public AgentIterator() {
			this.cursor = 0;
		}

		public boolean hasNext() {
			return this.cursor < agents.size();
		}

		public Agent next() {
			if(this.hasNext()) {
				Agent currAgent = agents.get(cursor);
				cursor ++;
				return currAgent;
			}
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * 
	 * @return true if all agents are at their target locations
	 */
	public boolean finished() {
		for (Agent a: agents) {
			if (!a.atTarget()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * play moves of agents one by one
	 * @param map
	 */
	public void playMove(Map map) {
		if (id == Constants.OFFENSIVE_TEAM) {
//			Collections.sort(agents, new DegreeOfFreedomComparator(map));
//			Collections.sort(agents, new DistanceComparator(map));
		}
		for (Agent agent: agents) {
			agent.makeMove(map);
			
		}
	}

	/**
	 * Assign targets to defending agents according to frequently used bottlenecks found in the map
	 */
	public void allocateTargetsBottlenecks(ArrayList<ArrayList<Location>> bottlenecks, boolean reallocate, int considerAgents) {
		targetAllocValidityCheck();
		ArrayList<Agent> agentsToAllocate;
		if (reallocate) {
			agentsToAllocate = new ArrayList<Agent>(agents);
		}
		else {
			agentsToAllocate = new ArrayList<Agent>();
			for (Agent a: agents) {
				if (!a.atTarget()) {
					agentsToAllocate.add(a);
				}
			}
		}
		ArrayList<ArrayList<Location>> paths = estimatePaths(considerAgents);
		HashMap<ArrayList<Location>, Integer> bottleneckPassFreqs = new HashMap<ArrayList<Location>, Integer>(); 
		for (ArrayList<Location> bneck: bottlenecks) {
			bottleneckPassFreqs.put(bneck, 0);
			for (ArrayList<Location> path : paths) {
				ArrayList<Location> intersect = (ArrayList<Location>) bneck.stream().filter(path::contains).collect(Collectors.toList()); // find an intersection of two lists
				if (!intersect.isEmpty()) {
					bottleneckPassFreqs.replace(bneck, bottleneckPassFreqs.get(bneck) + 1);
				}
			}
		}
		ArrayList<Location> bottleneck;
		while((bottleneck = getBottleNeckOfFreq(bottleneckPassFreqs, 10)) != null) {
			if (agentsToAllocate.size() < bottleneck.size()) {
				break; // while
			}
			assignBottleneck(bottleneck, agentsToAllocate); // items in agentsToAllocate are removed inside this call
			bottleneckPassFreqs.remove(bottleneck);
		}
		if (!agentsToAllocate.isEmpty()) {
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
	private void assignBottleneck(ArrayList<Location> bottleneck, ArrayList<Agent> agentsToAllocate) {
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
	private ArrayList<ArrayList<Location>> estimatePaths(int considerAgents) {
		ArrayList<ArrayList<Location>> paths = new ArrayList<ArrayList<Location>>();
		ArrayList<Location> targets = map.getTargets();
		for (Agent a : otherTeam) {
			Location t = targets.get(a.getId());  // guess a target by id
			LinkedList<Location> path = new BFS(considerAgents).findPath(a.getCurrentLocation(), t, map); 
			paths.add(new ArrayList<Location>(path));
		}
		return paths;
	}


	
	private void targetAllocValidityCheck() {
		if (this.id == Constants.OFFENSIVE_TEAM) {
			System.err.println("Offensive agents have determined targets");
			System.exit(1);
		}
	}
	
	/**
	 * return the numbere of agents that are currently standing at their targets
	 * @return
	 */
	public int finishedCnt() {
		int cnt = 0;
		for (Agent a: agents) {
			if (a.atTarget()) {
				cnt++;
			}
		}
		return cnt;
	}
	
	/**
	 * Compares two agents according to the number of empty locations surrounding their current location
	 * @author marika
	 *
	 */
	private  class DegreeOfFreedomComparator implements Comparator<Agent> {
		private Map map;
		public int compare(Agent o1, Agent o2) {
			int o1FreedomDeg = map.neighbors(o1.getCurrentLocation(), true).size();
			int o2FreedomDeg = map.neighbors(o2.getCurrentLocation(), true).size();
			return Integer.compare(o2FreedomDeg, o1FreedomDeg);
		}
		public DegreeOfFreedomComparator(Map map) {
			super();
			this.map = map;
		}
	}
	
	private  class DistanceComparator implements Comparator<Agent> {
		private Map map;
		public int compare(Agent o1, Agent o2) {
			int o1Dist = new BFS(Constants.CONSIDER_AGENTS_NONE).distsToLocation(map, map.getLocation(1, 1))[o1.getId()];
			int o2Dist = new BFS(Constants.CONSIDER_AGENTS_NONE).distsToLocation(map, map.getLocation(1, 1))[o2.getId()];
			return Integer.compare(o2Dist, o1Dist);
		}

		public DistanceComparator(Map map) {
			super();
			this.map = map;
		}
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
