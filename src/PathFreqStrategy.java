import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class PathFreqStrategy extends Strategy {

	private boolean minCD;
	private boolean useBrush;
	
	/**
	 * 
	 * @param multiStage shall we calculate targets in every step
	 * @param relocate - shall we reconsider those agents who already reached their destination
	 * @param considerAgents - shell we consider agents as obstacles
	 * @param minCD - shall we want the min cummulative distance
	 */
	public PathFreqStrategy(boolean multiStage, boolean relocate, int considerAgents, boolean minCD, boolean useBrush) {
		super(multiStage, relocate, considerAgents);
		this.minCD = minCD;
		this.useBrush = useBrush;
	}

	/**
	 * Assign targets to defending agents according to frequently used bottlenecks found in the map
	 */
	@Override
	public void allocateTargets() {
		ArrayList<Agent> agentsToAllocate = new ArrayList<Agent>();
		if (reallocate) { // if we want to reallocate, we will add all the agents to the list, including those who are at their targsts
			for (Agent a: defTeam) {
				agentsToAllocate.add(a);
			}
		}
		else { // if we don't want to reallocate, we will add only agents that haven't reached their targets.
			for (Agent a: defTeam) {
				if (!a.atTarget()) {
					agentsToAllocate.add(a);
				}
			}
		}
		ArrayList<Location> forbidden = new ArrayList<Location>();
		ArrayList<Location> freqLocs = new ArrayList<Location>();
		Location mostFreqLoc;
		// in this loop we always predict paths for all offensive agents and calculate the most frequent location with lowest cumulative distance
		// Centered in this location, we are expanding a square and trying to identify a bottleneck. If we find it, we 
		// assign defensive agents to the locations of the bottleneck and insert these locations to the forbidden list.
		// we hope that it should plug the bottleneck. Then we continue predicting the paths with the plugged bottleneck, and get new prediction
		// and possibly new bottleneck...
		do  {
			if (agentsToAllocate.isEmpty()) {
				break;
			}
			ArrayList<ArrayList<Location>> paths = estimatePaths(map, forbidden);
			HashMap<Location, Pair<Integer, Integer>> pathFreqsDists = calculatePathFreqDists(paths);
			mostFreqLoc = getMostFreqLoc(pathFreqsDists, freqLocs);
			freqLocs.add(mostFreqLoc);
			
			if (mostFreqLoc != null) { //
				System.err.println(mostFreqLoc.toString());
				Square square = new Square(mostFreqLoc);
				LinkedList<Location> bneck = square.expand(map, forbidden);
				if (bneck == null) { // no bottleneck found
					break;
				}

				if (!alreadyAssigned(bneck)) { // if a bottleneck that has already been used is again selected, just ignore it. We should continue with another top-freq node
					
					forbidden.addAll(bneck);
					ArrayList<ArrayList<Location>> updatedPaths = estimatePaths(map, forbidden); // check if the paths has changed
					if (!sameLengts(paths,updatedPaths)) { // asign only if the blocking of the new bottleneck will cause any change in the path estimation
						assignLocations(bneck, agentsToAllocate, map);	
					}
					else {
						// if the bottleneck is not eventually assigned, remove its locations from the forbidden lists
						// because they are not scheduled for blocking at all.
						forbidden.removeAll(bneck);
					}
				}
//				pathFreqsDists.remove(mostFreqLoc); // possibly delete ??
			}
		} while (mostFreqLoc != null);
		if (!agentsToAllocate.isEmpty()) {
			new RandomStrategy(multiStage, reallocate, considerAgents).allocateTargets(map, defTeam, offTeam);
		}
	}
	


	private boolean alreadyAssigned(LinkedList<Location> bneck) {
		for (Location loc: bneck) {
			if (!loc.isSomeDestination()) {
				return false;
			}
		}
		return false;
	}

	/**
	 * return the location with the most frequent visits and the lowest cummulative distance. This location must not be selected in any previous step, 
	 * which is ensured by the freqLocs list
	 * @param pathFreqsDists HashMap mapping Locations to the pair of their visit frequency and cummulative distance
	 * @return
	 */
	private Location getMostFreqLoc(HashMap<Location, Pair<Integer, Integer>> pathFreqsDists, ArrayList<Location> freqLocs) {
		ArrayList<Pair<Location, Pair<Integer, Integer>>> topFreqLocs = new ArrayList<Pair<Location, Pair<Integer, Integer>>>(); 
		// create the list of top frequency Locations
		int maxF = 0;
		Iterator<HashMap.Entry<Location, Pair<Integer, Integer>>> it = pathFreqsDists.entrySet().iterator();
		while (it.hasNext()) {
			HashMap.Entry<Location, Pair<Integer, Integer>> entry = (HashMap.Entry<Location, Pair<Integer, Integer>>)it.next();
			if (!freqLocs.contains(entry.getKey())) {
				int f = entry.getValue().getFirst();
				if (f > maxF) {
					topFreqLocs.clear();
					maxF = f;
					topFreqLocs.add(new Pair<Location, Pair<Integer, Integer>>(entry.getKey(), entry.getValue()));
				}
				if (f == maxF) {
					topFreqLocs.add(new Pair<Location, Pair<Integer, Integer>>(entry.getKey(), entry.getValue()));
				}
			}
		}
		//	From among the top frequency locations, choose the one with minimum cummulative distance
//		Location bestLoc = topFreqLocs.get(0).getFirst();
		Location bestLoc = null;
	if (!topFreqLocs.isEmpty()) bestLoc = topFreqLocs.get(0).getFirst();
		if (minCD) {
			int minCD = Constants.INFINITY; // for min distance
			for (Pair<Location, Pair<Integer, Integer>> tfl: topFreqLocs) { // should not be necessery as we will not add such an item to the topFreqList. The condition was moved above
				if (!freqLocs.contains(tfl.getFirst())) {
					int cd = tfl.getSecond().getSecond();
					if (cd < minCD) {
						minCD = cd;
						bestLoc = tfl.getFirst();
					}
				}
			}
		}
		else {
			int maxCD = 0; // for max distance
			for (Pair<Location, Pair<Integer, Integer>> tfl: topFreqLocs) {  // should not be necessery as we will not add such an item to the topFreqList. The condition was moved above
				if (!freqLocs.contains(tfl.getFirst())) {
					int cd = tfl.getSecond().getSecond();
					if (cd > maxCD) {
						maxCD = cd;
						bestLoc = tfl.getFirst();
					}
				}
			}
		}
		return bestLoc;
	}

	/**
	 * fill and return the HashMap mapping location to two numbers - frequency and cumulative distance
	 * @param paths
	 * @return
	 */
	private HashMap<Location, Pair<Integer, Integer>> calculatePathFreqDists(ArrayList<ArrayList<Location>> paths) {
		int[] distsToCenter = new BFS(0).distsToLocation(map, defTeam.getCenterPoint());
		HashMap<Location, Pair<Integer, Integer>> pathFreqsDists = new HashMap<Location, Pair<Integer, Integer>>();
		for (ArrayList<Location> path: paths) {
			for (Location loc: path) {
				
				Pair<Integer, Integer> vals = pathFreqsDists.get(loc);
				if (vals == null) { // add a new entry, if this Lo8cation does not have value
					pathFreqsDists.put(loc, new Pair<Integer, Integer>(1, distsToCenter[loc.getId()]));
//					pathFreqsDists.put(loc, new Pair<Integer, Integer>(1, path.indexOf(loc)));
				}
				else { // the entry exists, just update the values
					int oldF = pathFreqsDists.get(loc).getFirst(); // old frequency of the location loc
					int oldCD = pathFreqsDists.get(loc).getSecond(); // old cumulative distance of the locatoin loc
					if  (useBrush) {
						ArrayList<Location> neighbours = map.neighbors(loc, true, true);
						for (Location nbr: neighbours) {
							int oldFNbr = 0;
							int oldCDNbr = 0;
							if (pathFreqsDists.get(nbr) != null) {
								oldFNbr = pathFreqsDists.get(nbr).getFirst(); // old frequency of the location loc
								oldCDNbr = pathFreqsDists.get(nbr).getSecond();
							}
							pathFreqsDists.replace(nbr, new Pair<Integer, Integer>(oldFNbr + 1, oldCDNbr));		
						}
					}
					pathFreqsDists.replace(loc, new Pair<Integer, Integer>(oldF + 1, oldCD)); 
//					pathFreqsDists.replace(loc, new Pair<Integer, Integer>(oldF + 1, oldCD + path.indexOf(loc))); 
				}
			}
		}
		Iterator<HashMap.Entry<Location, Pair<Integer, Integer>>> it = pathFreqsDists.entrySet().iterator();
//		while (it.hasNext()) {
//			HashMap.Entry<Location, Pair<Integer, Integer>> entry = (HashMap.Entry<Location, Pair<Integer, Integer>>)it.next();
//			System.out.println(entry.getKey() + "  : " + entry.getValue().getFirst() + " , " + entry.getValue().getSecond());
//		}
		return pathFreqsDists;
	}
	
	/**
	 * Assign locations of a specified bottleneck to appropriate agents that still haven't been allocated
	 * @param bottleneck - locations to be assinged
	 * @param agentsToAllocate agents that still haven't been allocated and can be used
	 */
	private void assignLocation(Location loc, ArrayList<Agent> agentsToAllocate, Map map) {
		Collections.sort(agentsToAllocate, new DistToLocationComparator(map, loc)); // can be faster by placing in front of cycle, with a minor loss of accuracy
		Agent a = agentsToAllocate.remove(0);
		a.setTargetLocation(loc);
	}
	
	/**
	 * Assign locations of a specified bottleneck to appropriate agents that still haven't been allocated
	 * @param bottleneck - locations to be assinged
	 * @param agentsToAllocate agents that still haven't been allocated and can be used
	 */
	private void assignLocations(LinkedList<Location> locs, ArrayList<Agent> agentsToAllocate, Map map) {
		for (Location loc: locs) {
			if (agentsToAllocate.size() == 0) {
				return;
			}
			Collections.sort(agentsToAllocate, new DistToLocationComparator(map, loc)); // can be faster by placing in front of cycle, with a minor loss of accuracy
			Agent a = agentsToAllocate.remove(0);
			a.setTargetLocation(loc);
		}
	}

	public String toString() {
		return "SQUARE STRATEGY " + ( minCD ? "MinCD" : "MaxCD") + (useBrush ? "Brush" : "");
	}

}
