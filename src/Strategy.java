
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public abstract class Strategy {
	
	protected ArrayList<Location> targets;
	protected boolean multiStage;
	protected boolean firstRun;
	protected boolean reallocate;
	protected int considerAgents;
	protected Team defTeam;
	protected Team offTeam;
	protected Map map;
	protected int comCnt;
	protected static final double percentage = 0.1;
	
	public Strategy(boolean multiStage, boolean relocate, int considerAgents) {
		this.multiStage = multiStage;
		this.reallocate = relocate;
		this.considerAgents = considerAgents;
		this.firstRun = true;
	}
	
	// assign targets according to a certain strategy.
	public void allocateTargets( Map map, Team defTeam, Team offTeam) {
		this.defTeam = defTeam;
		this.offTeam = offTeam;
		this.map = map;
		this.targets = map.getTargets();
		if (firstRun || multiStage) {
			comCnt = (int) Math.ceil(defTeam.agentCnt() * percentage);
			firstRun = false;
			allocateTargets();
		}
	}
	
	/**
	 * randomly assign targets to offensive agents and calculate shortest paths
	 * @return
	 */
	protected ArrayList<ArrayList<Location>> estimatePaths(Map map, ArrayList<Location> forbidden) {
		ArrayList<ArrayList<Location>> paths = new ArrayList<ArrayList<Location>>();
		ArrayList<Location> targets = map.getTargets();
		int i = 0;
		for (Agent a : offTeam) {
			Location t = targets.get(i);  // guess a target by id
			i++;
			LinkedList<Location> path = new BFS(considerAgents).findPathWithout(a.getCurrentLocation(), t, forbidden, map); 
			paths.add(new ArrayList<Location>(path));
		}
		return paths;
	}
	
	
	protected abstract void allocateTargets();

	public void reNew() {
		firstRun = true;
	}
	
	/**
	 * allocate the remaining defenders, that are not yet assigned any target and that are supposed to be communicators.
	 * It is done by calculating the connected components of the already assigned targets and then finding those locations
	 * that cover the highest number of components. Among them, we select the one that maximizes the number of visible 
	 * targets in the component with minimum covered nodes.
	 */
	protected void allocateCommunicators() {
		ArrayList<Location> assignedTargets = new ArrayList<>();
		ArrayList<Agent> unallocatedAgents = new ArrayList<Agent>();
		for (Agent a: defTeam) { // the normal defenders (those that are already assigned a target should come first (TODO: confirm. )
			if (a.getTargetLocation() != null) {
				assignedTargets.add(a.getTargetLocation());
			}
			else {
				unallocatedAgents.add(a);
			}
		}
		ArrayList<ArrayList<Location>> conComps = determineConComps(assignedTargets); 			// find all connected components on assigned targets defined by the visibility graph 
		ArrayList<ArrayList<Location>> conCompsToCover = new ArrayList<ArrayList<Location>>(); 	// connected components to be covered
		conCompsToCover.addAll(conComps); 														// initially equal to the set of all CCs
		for (Agent a: unallocatedAgents) {
//		for (int i = communicatorFirstIdx; i < defTeam.agentCnt(); i++) {
			Pair<Location, ArrayList<ArrayList<Location>>> comPointAndCoveredComps = findComPoint(conCompsToCover);
			a.setTargetLocation(comPointAndCoveredComps.getFirst());
			conCompsToCover.removeAll(comPointAndCoveredComps.getSecond());
		}
	}
	
	/**
	 * Here we find the location that will be assigned as a target to the next defender who is supposed to be a communicator.
	 * It will be the locations that covers the maximum number of not yet covered connected components.
	 * @param conCompsToCover connected components that are not yet covered
	 * @return pair of Location and the connected components that would be covered if this location is captured by a defender-communicator
	 */
	private Pair<Location, ArrayList<ArrayList<Location>>> findComPoint(ArrayList<ArrayList<Location>> conCompsToCover) {
		ArrayList<ArrayList<Location>> maxCoveredComps = new ArrayList<ArrayList<Location>>();
		Location maxCoveringLoc = null;
		for (Location loc: map) {
			if (!loc.isObstacle()) {
				ArrayList<ArrayList<Location>> coveredComps = findCoveredComps(loc, conCompsToCover);
				if (coveredComps.size() > maxCoveredComps.size()) { // we found a best covering location so far
					maxCoveredComps = coveredComps;
					maxCoveringLoc = loc;
				}
			}
		}
		return new Pair<Location, ArrayList<ArrayList<Location>>>(maxCoveringLoc, maxCoveredComps);
	}

	/**
	 * determine the list of connected components that have at least one location visible for the input loc
	 * @param loc - the location we want to determine the number of covering CCs
	 * @param conCompsToCover the CCs we are interested in covering
	 * @return
	 */
	private ArrayList<ArrayList<Location>> findCoveredComps(Location loc, ArrayList<ArrayList<Location>> conCompsToCover) {
		ArrayList<ArrayList<Location>> coveredComps = new ArrayList<ArrayList<Location>>();
		for (ArrayList<Location> cc: conCompsToCover) {
			if (isVisibleFromLoc(cc, loc)) {
				coveredComps.add(cc);
			}
		}
		return coveredComps;
	}

	/**
	 * determine whether a connected component is visible from a given location
	 * @param cc
	 * @param loc
	 * @return
	 */
	private boolean isVisibleFromLoc(ArrayList<Location> cc, Location loc) {
		for (Location ccLoc: cc) {
			if (map.canSeeEachOther(ccLoc, loc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create arraylists of assigned targets that are located so that they form a connected component
	 * @param assignedTargets - list of assigned targets. If one cc is found, its locations are removed from this list
	 * @return
	 */
	private ArrayList<ArrayList<Location>> determineConComps(ArrayList<Location> assignedTargets) {
		ArrayList<ArrayList<Location>> conComps = new ArrayList<>();
		while (!assignedTargets.isEmpty()) {
			ArrayList<Location> visited = new ArrayList<>();
	        Stack<Location> stack= new Stack<Location>();
	        Location v = assignedTargets.get(0);
	        stack.push(v);
	        while (!stack.isEmpty()) {
	       	Location u = stack.pop();
	           if (!visited.contains(u)) {
	               visited.add(u);
	               ArrayList<Location> visNeighbours = map.getVisNeighbours(u, assignedTargets);
	               for (Location neigh: visNeighbours) {
	            	   if (!visited.contains(neigh)) {
	            		   stack.push(neigh);
	            	   }
	               }
	           }
	        }
	        conComps.add(visited);
	        assignedTargets.removeAll(visited);
		}
		return conComps;
	}

	protected boolean sameLengts(ArrayList<ArrayList<Location>> paths, ArrayList<ArrayList<Location>> updatedPaths) {
		for (int i = 0; i < paths.size(); i++) {
			if (paths.get(i).size() != updatedPaths.get(i).size()) {
				return false;
			}
		}
		return true;
	}
	
}
