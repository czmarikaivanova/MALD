
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
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
	protected int normalDefCnt;

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
	protected void allocateCommunicators(ArrayList<Agent> communicators) {
		ArrayList<Location> assignedTargets = new ArrayList<>();
		for (Agent a: defTeam) { // the normal defenders (those that are already assigned a target should come first (TODO: confirm. )
			if (a.getTargetLocation() != null) {
				assignedTargets.add(a.getTargetLocation());
			}
		}
		PriorityQueue<ArrayList<Location>> conComps = determineConComps(assignedTargets); 			// find all connected components on assigned targets defined by the visibility graph 
		ArrayList<ArrayList<Location>> conCompsToCover = new ArrayList<ArrayList<Location>>(); 	// connected components to be covered
		conCompsToCover.addAll(conComps); 														// initially equal to the set of all CCs
		while (!communicators.isEmpty()) {						// while I have some communicators left
			while (!conCompsToCover.isEmpty()) {				// while there are CCs to cover
				Pair<Location, ArrayList<ArrayList<Location>>> comPointAndCoveredComps = findComPoint(conCompsToCover);  // find a location that covers most CCs and the potentially covered CCs
				Location targetToAssign = comPointAndCoveredComps.getFirst();											  
				Agent closestAgent = getClosestAgent(communicators, targetToAssign);									 // find the available communicator closest to the location found above
				closestAgent.setTargetLocation(targetToAssign);															 // set target of the available communicator 
				assignedTargets.add(targetToAssign);																	 // insert the new target to the set of already assigned targets
				conCompsToCover.removeAll(comPointAndCoveredComps.getSecond());											 // Remove the components covered by the target from the set of CCs to coveer
				communicators.remove(closestAgent);																		 // Remove the allocated communicater from the set of available communicators
				if (communicators.isEmpty()) { // this can happen in the inner loop		
					break;
				}
			}
			conComps = determineConComps(assignedTargets);		// at this point 
			conCompsToCover.addAll(conComps);
		}
	}
	
	/**
	 * find an agent that is closest ot the given location
	 * @param communicators set of available agents
	 * @param targetToAssign given target location
	 * @return
	 */
	private Agent getClosestAgent(ArrayList<Agent> communicators, Location targetToAssign) {
		Agent closestAgent = communicators.get(0);
		int minDst = Constants.INFINITY;
		for (Agent a: communicators) {
			int dst = map.getDst(a.getCurrentLocation(), targetToAssign);
			if (dst < minDst) {
				minDst = dst;
				closestAgent = a;
			}
		}
		return closestAgent;
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
				if (coveredComps.size() == maxCoveredComps.size()) { // if we find a  location that is as good as a previously found one, we will update the values with probability 1/2.
					Random rnd = new Random(1);
					if (rnd.nextInt(2) == 1) {
						maxCoveredComps = coveredComps;
						maxCoveringLoc = loc;
					}
				}
				else if (coveredComps.size() > maxCoveredComps.size()) { // we found a best covering location so far
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
	 * @param assignedTargetsCopy - list of assigned targets. If one cc is found, its locations are removed from this list
	 * @return
	 */
	protected PriorityQueue<ArrayList<Location>> determineConComps(ArrayList<Location> assignedTargets) {
		ArrayList<Location> assignedTargetsCopy = new ArrayList<>(assignedTargets);
		PriorityQueue<ArrayList<Location>> conComps = new PriorityQueue<ArrayList<Location>>(new ListLengthComparator());
		while (!assignedTargetsCopy.isEmpty()) {
			ArrayList<Location> visited = new ArrayList<>();
	        Stack<Location> stack= new Stack<Location>();
	        Location v = assignedTargetsCopy.get(0);
	        stack.push(v);
	        while (!stack.isEmpty()) {
	       	Location u = stack.pop();
	           if (!visited.contains(u)) {
	               visited.add(u);
	               ArrayList<Location> visNeighbours = map.getVisNeighbours(u, assignedTargetsCopy);
	               for (Location neigh: visNeighbours) {
	            	   if (!visited.contains(neigh)) {
	            		   stack.push(neigh);
	            	   }
	               }
	           }
	        }
	        conComps.add(visited);
	        assignedTargetsCopy.removeAll(visited);
		}
		return conComps;
	}
	

	/**
	 * divide the team of defenders into two lists - normal defenders and communicators. 
	 * The ration between these two sets is given by the constant "percentage" and is set to 0.9
	 * @return
	 */
	protected Pair<ArrayList<Agent>, ArrayList<Agent>> divideAgentsIntoDefAndComcomCnt() {
		ArrayList<Agent> normalDefenders = new ArrayList<>();
		ArrayList<Agent> communicators = new ArrayList<>();
		int i = 0;
		int normalDefCnt = (int) Math.floor(defTeam.agentCnt() * 0.9);
		for (Agent a: defTeam) {
			if (i < normalDefCnt) {
				normalDefenders.add(a);
			}
			else {
				communicators.add(a);
			}
			i++;
		}
		return new Pair<>(normalDefenders, communicators);
	}

	protected boolean sameLengts(ArrayList<ArrayList<Location>> paths, ArrayList<ArrayList<Location>> updatedPaths) {
		for (int i = 0; i < paths.size(); i++) {
			if (paths.get(i).size() != updatedPaths.get(i).size()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Comparator that compares two lists according to their length
	 * @author marika
	 *
	 */
	private  class ListLengthComparator implements Comparator<ArrayList>
	{
	    @Override
	    public int compare(ArrayList x, ArrayList y)
	    {
	        // Assume neither string is null. Real code should
	        // probably be more robust
	        // You could also just return x.length() - y.length(),
	        // which would be more efficient.
	        if (x.size() < y.size())
	        {
	            return 1;
	        }
	        if (x.size() > y.size())
	        {
	            return -1;
	        }
	        return 0;
	    }
	}
	
}
