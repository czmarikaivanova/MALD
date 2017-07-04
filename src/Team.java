import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

public class Team implements Iterable<Agent> {
	private ArrayList<Agent> agents;
	private int id;
	private Random randomGen;
	
	/**
	 * constructor
	 * @param id
	 */
	public Team(int id) {
		this.id = id;
		agents = new ArrayList<Agent>();
		randomGen = new Random(10);
	}
	
	/**
	 * insert an agent. It will happen only at the beginning
	 * @param a
	 */
	public void add(Agent a) {
		a.setAlg(new LRAstar());
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
	
	public void allocateTargets() {
		for (Agent agent: agents) {
			// TODO
		}
	}

	/**
	 * allocate targets to agents randomly
	 * @param targets
	 */
	public void allocateTargetsRandom(ArrayList<Location> targets) {
		Collections.shuffle(targets, randomGen);	
		int i = 0;
		for (Agent agent: agents) {
			if (i >= targets.size()) {  // assign always only one target to one agent
				return;
			}
			agent.setTargetLocation(targets.get(i));
			i++;
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
			int o1Dist = new BFS().distsToLocation(map, map.getLocation(1, 1))[o1.getId()];
			int o2Dist = new BFS().distsToLocation(map, map.getLocation(1, 1))[o2.getId()];
			return Integer.compare(o2Dist, o1Dist);
		}

		public DistanceComparator(Map map) {
			super();
			this.map = map;
		}

	}
	
}
