import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.text.html.HTMLDocument.Iterator;

public class Agent {
	private int id;
	private int team;
	private Location myCurrentLocation;
	private Location initLocation;
	private Location targetLocation;
	private Algorithm algorithm;
	private LinkedList<Location> path;
	
	public Agent(int id, int team, Location initLocation, Location targetLocation) {
		super();
		this.id = id;
		this.team = team;
		this.initLocation = initLocation;
		this.targetLocation = targetLocation;
		this.myCurrentLocation = initLocation;
	}
	
	/**
	 * actually make a move in the data structure
	 * must be legal
	 * @param map - current configuration
	 * @param newLoc - new location of the agent
	 */
	private void makeMove(Map map, Location newLoc) {
		if (validMove(newLoc)) {
			myCurrentLocation.setAgent(null);
			newLoc.setAgent(this);
			myCurrentLocation = newLoc;
		}
		else {
			System.err.println("Invalid move!");
			System.err.println(newLoc);
			System.err.println(myCurrentLocation);
			System.exit(1);
		}
	}
	
	/**
	 * Either find the next move in the path variable, or call a search algorithm for finding the path
	 * Then call a private method for the actual move to take place
	 * @param map
	 */
	public void makeMove(Map map) {
		if (path == null || path.size() == 0) {
			path = algorithm.findPath(myCurrentLocation, targetLocation, map);
		}
		Location newLoc =  path.remove();
		makeMove(map, newLoc);
	}
	
	/**
	 * check if the new location is adjacent to my location
	 * @param newLoc
	 * @return
	 */
	private boolean validMove(Location newLoc) {
		// staying at a position is always legal
		if (newLoc.getX() == myCurrentLocation.getX() && newLoc.getY() == myCurrentLocation.getY()) {
			return true;
		}
		// Entering an obstacle or an occupied location is illegal. The second condition is satisfied even 
		// if the agent does not move (is occupied by itself) but that is covered in the previous case
		if (newLoc.isObstacle() || newLoc.getAgent() != null) {
			return false;
		}
		// Moving up, down, left, right are legal moves, as long as the locations are empty
		if (newLoc.getX() == myCurrentLocation.getX() + 1 && newLoc.getY() == myCurrentLocation.getY()) {
			return true;
		}
		if (newLoc.getX() == myCurrentLocation.getX() - 1 && newLoc.getY() == myCurrentLocation.getY()) {
			return true;
		}
		if (newLoc.getX() == myCurrentLocation.getX() && newLoc.getY() == myCurrentLocation.getY() + 1) {
			return true;
		}
		if (newLoc.getX() == myCurrentLocation.getX() && newLoc.getY() == myCurrentLocation.getY() - 1) {
			return true;
		}
		return false;
	}


	/**
	 * return the team where this agent belongs
	 * @return
	 */
	public int getTeam() {
		return this.team;
	}
	
	/**
	 * return the target location of this agent
	 * @return 
	 */
	public Location getTargetLocation() {
		return targetLocation;
	}
	
	public Location getCurrentLocation() {
		return myCurrentLocation;
	}
	
	public String toString() {
		String teamStr = team == Constants.DEFENSIVE_TEAM ? "D" : "O";
		return id + " [" + myCurrentLocation.getX() + ", " + myCurrentLocation.getY() + "]" + teamStr; 
	}

	public int getId() {
		return id;
	}

	public void setAlg(Algorithm alg) {
		this.algorithm = alg;
		
	}

	
}
