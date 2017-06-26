import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Team implements Iterable<Agent> {
	private ArrayList<Agent> agents;
	private int id;
	
	/**
	 * constructor
	 * @param id
	 */
	public Team(int id) {
		this.id = id;
		agents = new ArrayList<Agent>();
	}
	
	/**
	 * insert an agent. It will happen only at the beginning
	 * @param a
	 */
	public void add(Agent a) {
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

	public Iterator<Agent> iterator() {
		return new AgentIterator();
	}
	
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

	public boolean finished() {
		for (Agent a: agents) {
			if (!a.getCurrentLocation().equals(a.getTargetLocation())) {
				return false;
			}
		}
		System.out.println("All agents reached their destination.");
		return true;
	}
	
	
	
	
}
