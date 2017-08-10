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
	private Location centerPoint;
	
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

	public int agentCnt() {
		return agents.size();
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
			int o1FreedomDeg = map.neighbors(o1.getCurrentLocation(), true, false).size();
			int o2FreedomDeg = map.neighbors(o2.getCurrentLocation(), true, false).size();
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
	 * shuffle agents order
	 */
	public void shuffle() {
		Collections.shuffle(agents, new Random(111));
	}

	public void calculateCenterPoint() {
		int x = 0;
		int y = 0;
		for (Agent a: agents) {
			x += a.getCurrentLocation().getX();
			y += a.getCurrentLocation().getY();
		}
		x = x / agentCnt();
		y = y / agentCnt();
		centerPoint = map.getLocation(x, y);
	}

	public Location getCenterPoint() {
		return centerPoint;
	}
	
//	/**
//	 * Compares two agents accordint to their distance to a specified location
//	 * @author marika
//	 *
//	 */
//	private class DistToLocationComparator implements Comparator<Agent> {
//		BFS bfs;
//		Map map;
//		Location loc;
//		
//		public DistToLocationComparator(Map map, Location loc) {
//			super();
//			this.map = map;
//			this.loc = loc;
//			bfs = new BFS(Constants.CONSIDER_AGENTS_NONE);
//		}
//
//		@Override
//		public int compare(Agent o1, Agent o2) {
//			int d1 = bfs.minPathLength(o1.getCurrentLocation(), loc, map);
//			int d2 = bfs.minPathLength(o2.getCurrentLocation(), loc, map);
//			return Integer.compare(d1, d2);
//		}
//		
//	}

}
