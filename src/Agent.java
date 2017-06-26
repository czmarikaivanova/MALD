import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.text.html.HTMLDocument.Iterator;

public class Agent {
	private int id;
	private int team;
	private Location myCurrentLocation;
	private Location initLocation;
	private Location targetLocation;
	
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
	 * @param direction
	 */
	public void makeMove(Map map, int direction) {
		int dx = 0;
		int dy = 0;
		if (direction == Constants.MOVE_UP) {
			dx = 0;
			dy = 1;
		}
		if (direction == Constants.MOVE_DOWN) {
			dx = 0;
			dy = -1;
		}
		if (direction == Constants.MOVE_LEFT) {
			dx = -1;
			dy = 0;
		}
		if (direction == Constants.MOVE_RIGHT) {
			dx = 1;
			dy = 0;
		}
		map.getLocation(myCurrentLocation.getX(), myCurrentLocation.getY()).setAgent(null);
		myCurrentLocation = map.getLocation(myCurrentLocation.getX() + dx, myCurrentLocation.getY() + dy);
		map.getLocation(myCurrentLocation.getX(), myCurrentLocation.getY()).setAgent(this);
	}
	
	public void makeMove(Map map, Location newLoc) {
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

	public Location BFS(Map map, Location pathTarget) { // todo: avoid next move to be occupied, but all other on the paths can have agents (no obstacles though)
		if (team == Constants.DEFENSIVE_TEAM) {
			return null; // defensive agent does not use BFS
		}
		if (pathTarget == myCurrentLocation) {
			return myCurrentLocation; // don't move if you have arrived there
		}
		HashMap<Location, Boolean> flags = new HashMap<Location, Boolean>();
		HashMap<Location, Location> prevs = new HashMap<Location, Location>();
		for (Location loc: map) { // INIT
			flags.put(loc, false);
			prevs.put(loc, null);
		}
		LinkedList<Location> queue = new LinkedList<Location>();
		queue.add(myCurrentLocation);
		flags.put(myCurrentLocation, true);
		while (!queue.isEmpty()) { // BFS SEARCH
			Location loc = queue.remove();
			for (int dir1 = -1; dir1 <= 1; dir1++) {
				for (int dir2 = -1; dir2 <= 1; dir2++) {
					if (Math.abs(dir1) != Math.abs(dir2)) { // do not include the current node in BFS and do not consider diagonals
						int x = loc.getX() + dir1;
						int y = loc.getY() + dir2;
						if (x >= 0 && x < map.getWidth() && y >= 0 && y < map.getHeight()) { // we should not get out of the map
							Location adjLoc = map.getLocation(x, y);
							if (!adjLoc.isObstacle() && flags.get(adjLoc).equals(Boolean.FALSE)) { // it will not be null
								flags.put(adjLoc, true);
								prevs.put(adjLoc, loc);
								queue.add(adjLoc);
							}
						}
					}
				}
			}
		}
		Location loc = pathTarget;			// retrieve the next location
		if (loc.equals(myCurrentLocation)) { // if already in target, stay there
			return myCurrentLocation;
		}
		Location prevLoc = prevs.get(loc);
		while (!prevLoc.equals(myCurrentLocation)) {
			loc = prevLoc;
			prevLoc = prevs.get(loc);
		}
		if (loc.getAgent() != null) { // if I am about to move to an adjacent location that is already occupied, stay on my place
			return myCurrentLocation; 
		}
		return loc;
	}

	private void writeMap(HashMap<Location, Location> prevs) {
		for (HashMap.Entry<Location, Location> e: prevs.entrySet()) {
			System.out.println("key: " + e.getKey() + " value: " + prevs.get(e.getKey())) ;
		}
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
	
}
