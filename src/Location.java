import java.util.ArrayList;

public class Location {
	private int x;
	private int y;
	private boolean obstacle;
	private ArrayList<Agent> destinationsForAgents;
	private Agent agent;
	
	
	public Location(int x, int y) {
		super();
		this.x = x;
		this.y = y;
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
		return "[" + x + "," + y +"]" + (obstacle ? "O" : "");
	}


}
