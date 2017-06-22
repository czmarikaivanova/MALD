
public class Location {
	private int x;
	private int y;
	private boolean obstacle;
	private Agent agent;
	
	
	public Location(int x, int y, boolean obstacle, Agent agent) {
		super();
		this.x = x;
		this.y = y;
		this.obstacle = obstacle;
		this.agent = agent;
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



}
