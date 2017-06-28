import java.util.ArrayList;

public class Location {
	private int x;
	private int y;
	private int id;
	private boolean obstacle;
	private ArrayList<Agent> destinationsForAgents;
	private Agent agent;
	
	public Location(int x, int y, int id) {
		super();
		this.x = x;
		this.y = y;
		this.id = id;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public int getId() {
		return id;
	}

	public boolean isObstacle() {
		return obstacle;
	}
	
	public void setObstacle() {
		obstacle = true;
	}
	
	public void setAgentDest(Agent agent) {
		destinationsForAgents.add(agent);
	}
	
	public boolean isSomeDestination() {
		return !destinationsForAgents.isEmpty();
	}
	
	public boolean isDestinationOf(Agent agent) {
		return destinationsForAgents.contains(agent);
	}

	public String toString() {
		return id + ":[" + x + "," + y +"]" + (obstacle ? "X" : "");
	}

	public boolean isNeighbour(Location loc) {
		if (this.getX() == loc.getX() && Math.abs(this.getY() - loc.getY()) == 1) {
			return true;
		}
		if (this.getY() == loc.getY() && Math.abs(this.getX() - loc.getX()) == 1) {
			return true;
		}
		return false;
	}
	

}
