import java.util.ArrayList;

public class Location {
	private int x;
	private int y;
	private float cx;
	private float cy;
	private int id;
	private boolean obstacle;
	private ArrayList<Agent> destinationsForAgents;
	private Agent agent;
	private int linId;
	
	
	public Location(int x, int y, int id) {
		super();
		this.x = x;
		this.y = y;
		this.id = id;
		destinationsForAgents = new ArrayList<Agent>();
		this.cx = (float) (x + 0.5);
		this.cy = (float) (y + 0.5);
		this.linId = -1; // in case of a free location it will be changed to something non negative
	}

	/**
	 * get an agent currently standing at this location
	 * @return
	 */
	public Agent getAgent() {
		return agent;
	}

	/**
	 * put an agent into this location
	 * @param agent
	 */
	public void setAgent(Agent agent) {
		if (isObstacle()) {
			System.err.println("Adding an agent into an obstacle! " + toString());
			System.exit(1);
		}
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
	
	public float getCX() {
		return cx;
	}
	
	public float getCY() {
		return cy;
	}

	/**
	 * check whether this location is an obstacle
	 * @return
	 */
	public boolean isObstacle() {
		return obstacle;
	}
	
	/**
	 * set this location to be an obstacle
	 */
	public void setObstacle() {
		obstacle = true;
	}
	
	/**
	 * set an agent to whom this location is a destination
	 * @param agent
	 */
	public void setAgentDest(Agent agent) {
		destinationsForAgents.add(agent);
	}
	
	/**
	 * 
	 * @return true if this location is some agent's destination
	 */
	public boolean isSomeDestination() {
		return !destinationsForAgents.isEmpty();
	}
	
	/**
	 * 
	 * @param agent - to be checked
	 * @return true if @param agent has this location as a destination
	 */
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
	
	
	public boolean equals(Location loc) {
		if (loc == null) {
			return false;
		}
		return (loc.x == this.x) && (loc.y == this.y);
	}

	public int getLinId() {
		return linId;
	}
	
	public void setLinId(int linId) {
		this.linId = linId;
	}
	
	
}
