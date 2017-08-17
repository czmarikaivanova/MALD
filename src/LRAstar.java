import java.util.List;
import java.util.Random;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class LRAstar extends Algorithm {

	private LinkedList<Location> openList;
	private LinkedList<Location> closedList;
	private HashMap<Location, Pair<Integer, Integer>> ghValues;
	private HashMap<Location, Location> prevs = new HashMap<Location, Location>();
	private int[] dstsToTarget;
	private Random rndGenerator;
	private boolean firstEmpty;
	
	
	public LRAstar(boolean firstEmpty) {
		super();
		this.rndGenerator = new Random(100);
		this.firstEmpty = firstEmpty;
	}

	public LinkedList<Location> findPath(Location start, Location target, Map map) {
		openList = new LinkedList<Location>();
		closedList = new LinkedList<Location>();
		ghValues = new HashMap<Location, Pair<Integer,Integer>>();
		prevs = new HashMap<Location, Location>();
			dstsToTarget = new BFS(Constants.CONSIDER_AGENTS_NONE).distsToLocation(map, target);
		for (Location loc: map) {
			int dst = dstsToTarget[loc.getId()];
			ghValues.put(loc, new Pair<Integer, Integer>(Constants.INFINITY, dst));
		}
		ghValues.put(start, new Pair<Integer, Integer>(0,0));
		openList.add(start);
		int tms = 0;
		while (!openList.isEmpty()) {
			Location q = extractMin();
			boolean firstStep = tms == 0;
			boolean mustBeConnected = firstStep && q.getAgent().getTeam() == Constants.DEFENSIVE_TEAM;
			ArrayList<Location> neighbours = map.neighbors(q, firstStep && firstEmpty, false, mustBeConnected);
			for (Location neighbour: neighbours) { // breakpoint only if defenders' turn
				if (neighbour.equals(target)) {
					prevs.put(target, q);
					return constructPath(map, start, target);
				}
				if (neighbour.getAgent() == null || neighbour.getAgent().getTeam() == start.getAgent().getTeam() && !neighbour.isSomeDestination()) {
					int distanceToMinNode = ghValues.get(q).getFirst();
					int n_g = distanceToMinNode + 1;
					int n_h = dstsToTarget[neighbour.getId()];
					ghValues.put(neighbour, new Pair<Integer, Integer>(n_g, n_h));
					 
	                 int alt = n_g + ghValues.get(neighbour).getSecond();
	                 int fValofN = ghValues.get(neighbour).getFirst() + ghValues.get(neighbour).getSecond();
	                 if (openList.contains(neighbour) && fValofN <= alt) {
	                     continue;
	                 }                        
	                 if (closedList.contains(neighbour) && fValofN <= alt) {
	                     continue;
	                 }
	                 ghValues.put(neighbour, new Pair<Integer, Integer>(n_g, dstsToTarget[neighbour.getId()]));
	                 openList.add(neighbour);
	                 prevs.put(neighbour, q);
				}
			}
			closedList.add(q);
			tms++;
		}
		return constructPath(map, start, target);
	}
	
	// extrat a node with minimum distance
    protected Location extractMin() {
        if (openList.isEmpty()) {
            throw new RuntimeException("Cannot extractMin from empty nodeList");
        }
        // distance for the closest node
        int min = Constants.INFINITY; 
        Location closestLoc = openList.get(0); // get first element
        // current distance between source and actual node
        for (Location loc: openList) {
//            int dist = loc.getF_val();
        	int dist = ghValues.get(loc).getFirst() + ghValues.get(loc).getSecond();
            if (dist < min) {
                closestLoc = loc;
                min = dist;
            }
        }
        openList.remove(closestLoc);
        /*
        if (closestNode == null) {
            System.err.println("NELZE EXTRAHOVAT MINIMUM");
        }
        else {
            System.out.println("Extrahovano minimum: " + closestNode.getId());
        }*/
        return closestLoc;
    }
    
    /**
     * add vertex to open/close list
     * @param list list where the vertex shall be inserted
     * @param v vertex to be inserted
     */
    private void addToList(List<Location> list, Location loc) {
        if (!list.contains(loc)) {
            list.add(loc);
        }		
	}
    
    private LinkedList<Location> constructPath(Map map, Location start, Location target) {
    	LinkedList<Location> constructedPath = new LinkedList<Location>();
    	constructedPath.addFirst(target);
		Location loc = target;			// retrieve the next location
		Location prevLoc = prevs.get(loc); // has to exist, because the case where the agent has reached the target is treated at the beginning
		if (prevLoc == null) {
			return randomMove(map, start); // no path currently exists.
		}
		while (!prevLoc.equals(start)) {
			loc = prevLoc;	
			constructedPath.add(0, loc); // add next location to the beginning
			prevLoc = prevs.get(loc);
		}
		return constructedPath;
    }

    /**
     * random move must lead to a connected position. Therefore last parameter true
     * @param map
     * @param loc
     * @return
     */
	private LinkedList<Location> randomMove(Map map, Location loc) {
		LinkedList<Location> rndMove = new LinkedList<Location>();
		ArrayList<Location> neighbors = map.neighbors(loc, true, false, true); 
		neighbors.add(loc);
		
		rndMove.add(neighbors.get(rndGenerator.nextInt(neighbors.size())));
		return rndMove;
	}
}

