import java.util.List;
import java.util.Random;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class LRAstar extends Algorithm {

	LinkedList<Location> openList;
	LinkedList<Location> closedList;
	HashMap<Location, Pair<Integer, Integer>> ghValues;
	HashMap<Location, Location> prevs = new HashMap<Location, Location>();
	
	public LinkedList<Location> findPath(Location start, Location target, Map map) {
		LinkedList<Location> path = new LinkedList<Location>();
		openList = new LinkedList<Location>();
		closedList = new LinkedList<Location>();
		ghValues = new HashMap<Location, Pair<Integer,Integer>>();
		prevs = new HashMap<Location, Location>();
		for (Location loc: map) {
			ghValues.put(loc, new Pair<Integer, Integer>(Constants.INFINITY, map.getDistance(start, target)));
		}
		ghValues.put(start, new Pair<Integer, Integer>(0,0));
		openList.add(start);
		int tms = 0;
		while (!openList.isEmpty()) {
			Location q = extractMin();
			ArrayList<Location> neighbours = map.neighbors(q, tms == 0);
			for (Location neighbour: neighbours) {
				if (neighbour.equals(target)) {
					prevs.put(target, q);
					return constructPath(map, start, target);
				}
				ghValues.put(neighbour, new Pair<Integer, Integer>(ghValues.get(q).getFirst() + map.getDistance(q, neighbour), map.getDistance(target, neighbour)));
				 int distanceToMinNode = ghValues.get(q).getFirst();
				 int distanceToN = distanceToMinNode + 1; // all edges have value 1 
                 int alt = distanceToN + ghValues.get(neighbour).getSecond();
                 int fValofN = ghValues.get(neighbour).getFirst() + ghValues.get(neighbour).getSecond();
                 if (openList.contains(neighbour) && fValofN <= alt) {
                     continue;
                 }                        
                 if (closedList.contains(neighbour) && fValofN <= alt) {
                     continue;
                 }
                 ghValues.put(neighbour, new Pair<Integer, Integer>(distanceToN, map.getDistance(neighbour, target)));
                 openList.add(neighbour);
                 prevs.put(neighbour, q);
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

	private LinkedList<Location> randomMove(Map map, Location loc) {
		LinkedList<Location> rndMove = new LinkedList<Location>();
		ArrayList<Location> neighbors = map.neighbors(loc, true);
		neighbors.add(loc);
		Random rnd = new Random();
		rndMove.add(neighbors.get(rnd.nextInt(neighbors.size())));
		return rndMove;
	}
}

