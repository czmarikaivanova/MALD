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
	private Random randomGen;
	private Map map;
	private Team otherTeam;
	
	/**
	 * constructor
	 * @param id
	 */
	public Team(int id, Map map, Team otherTeam) {
		this.id = id;
		agents = new ArrayList<Agent>();
		randomGen = new Random(10);
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
	 * Allocate the targets to the defensive agents as follows:
	 * for each agent in the order as the iterator returns
	 * 		find the closest target from the list of available targets
	 * 		assign the target to the agent and remove the target from the list of targets
	 * 
	 * TODO: if there is more defensive agents than targets, we should still assign something. 
	 * 		Right now it would return null.
	 * @param targets
	 */
	public void allocateTargetsRndOrderGreedy() {
		targetAllocValidityCheck();
		ArrayList<Location> availableTargets = new ArrayList<Location>(targets); 
		for (Agent agent: agents) {
			if (availableTargets== null || availableTargets.size() == 0) {
				System.err.println("No more available targets");
				System.exit(1);
			}
			Location myNewTarget = agent.getClosestLocation(availableTargets);
			agent.setTargetLocation(myNewTarget);
			availableTargets.remove(myNewTarget);
		}
	}

	/**
	 * allocate targets to agents randomly
	 * @param targets
	 */
	public void allocateTargetsRandom() {
		targetAllocValidityCheck();
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
	
	
	public void allocateTargetsBottlenecks() {
		targetAllocValidityCheck();
		ArrayList<ArrayList<Location>> bottlenecks = findBottlenecks(3);
		ArrayList<ArrayList<Location>> paths = estimatePaths();
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
		for (ArrayList<Location> bneck: bottlenecks) {
			System.out.println(bneck.toString() + " paths : " + bottleneckPassFreqs.get(bneck));
		}
	}
	
	private ArrayList<ArrayList<Location>> estimatePaths() {
		ArrayList<ArrayList<Location>> paths = new ArrayList<ArrayList<Location>>();
		ArrayList<Location> targets = map.getTargets();
		for (Agent a : otherTeam) {
			Location t = targets.get(a.getId());  // guess a target by id
			LinkedList<Location> path = new BFS(Constants.CONSIDER_AGENTS_NONE).findPath(a.getCurrentLocation(), t, map); 
			paths.add(new ArrayList<Location>(path));
		}
		return paths;
	}



	private ArrayList<ArrayList<Location>> findBottlenecks(int size) {
		ArrayList<ArrayList<Location>> bottleneckList = new ArrayList<ArrayList<Location>>();
		// check rows
		for (int i = 0; i < map.getHeight(); i++) {
			for (int j = 1; j < map.getWidth()-size; j++) {
				if(isEmptyWinRow(i, j, size) && map.getLocation(i, j - 1).isObstacle() && map.getLocation(i, j + size).isObstacle()) { // check for an empty sequennce of locations and whether it is surrounded by obstacles
					ArrayList<Location> bneck = new ArrayList<Location>();
					for (int k = 0; k < size; k++) {
						bneck.add(map.getLocation(i, j + k));
					}
					bottleneckList.add(bneck);
				}
			}
		}
		// check columns
		for (int j = 0; j < map.getWidth(); j++) {
			for (int i = 1; i < map.getHeight()-size; i++) {

				if(isEmptyWinCol(j, i, size) && map.getLocation(i - 1, j).isObstacle() && map.getLocation(i + size, j).isObstacle()) { // check for an empty sequennce of locations and whether it is surrounded by obstacles
					ArrayList<Location> bneck = new ArrayList<Location>();
					for (int k = 0; k < size; k++) {
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

}
