
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class BottleneckImprovedStrategy extends Strategy {

	ArrayList<ArrayList<Location>> bottlenecks;
	private int minF;
	
	public BottleneckImprovedStrategy(boolean multiStage, boolean reallocate, int considerAgents) {
		super(multiStage, reallocate, considerAgents);
		this.bottlenecks = null;
		minF = 5;
	}

	/**
	 * Assign targets to defending agents according to frequently used bottlenecks found in the map
	 */
	@Override
	public void allocateTargets() {
		if (bottlenecks == null) { // first call of this method
			bottlenecks = new ArrayList<ArrayList<Location>>();
			bottlenecks.addAll(findBottlenecks(1));
			bottlenecks.addAll(findBottlenecks(2));
			bottlenecks.addAll(findBottlenecks(3));
			bottlenecks.addAll(findBottlenecks(4));
			bottlenecks.addAll(findBottlenecks(5));
		}
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
		ArrayList<Location> bottleneck;
		do  {
			if (agentsToAllocate.size() < minF) {
				break;
			}
			ArrayList<ArrayList<Location>> paths = estimatePaths(map, forbidden);
			HashMap<ArrayList<Location>, Integer> bottleneckPassFreqs = calculateBottleneckFreqs(paths); 
			bottleneck = getBottleNeckOfFreq(bottleneckPassFreqs, minF);
			if (bottleneck != null) {
				forbidden.addAll(bottleneck);
				
//				ArrayList<ArrayList<Location>> updatedPaths = estimatePaths(map, forbidden); // check if the paths has changed
//				if (!sameLengts(paths,updatedPaths)) { // asign only if the blocking of the new bottleneck will cause any change in the path estimation
					assignBottleneck(bottleneck, agentsToAllocate, map);	
//				}
//				else {
					// if the bottleneck is not eventually assigned, remove its locations from the forbidden lists
					// because they are not scheduled for blocking at all.
//					forbidden.removeAll(bottleneck );
//				}
				
				bottleneckPassFreqs.remove(bottleneck);
			}
			else {
				break;
			}
			
		} while (bottleneck != null);
		if (!agentsToAllocate.isEmpty()) {
			new RandomStrategy(multiStage, reallocate, considerAgents).allocateTargets(map, defTeam, offTeam);
		}
	}
	
	private HashMap<ArrayList<Location>, Integer> calculateBottleneckFreqs(ArrayList<ArrayList<Location>> paths) {
		HashMap<ArrayList<Location>, Integer> bottleneckPassFreqs = new HashMap<ArrayList<Location>, Integer>(); 
		for (ArrayList<Location> bneck: bottlenecks) {
			bottleneckPassFreqs.put(bneck, 0);
			for (ArrayList<Location> path : paths) {
//				ArrayList<Location> intersect = (ArrayList<Location>) bneck.stream().filter(path::contains).collect(Collectors.toList()); // find an intersection of two lists
				if (hasCommonElements(bneck, path)) {
					bottleneckPassFreqs.replace(bneck, bottleneckPassFreqs.get(bneck) + 1);
				}
			}
		}
		for (ArrayList<Location> bneck: bottlenecks) {
			System.out.println(bneck.toString() + " paths : " + bottleneckPassFreqs.get(bneck));
		}
		return bottleneckPassFreqs;
	}
	
	/**
	 * Assign locations of a specified bottleneck to appropriate agents that still haven't been allocated
	 * @param bottleneck - locations to be assinged
	 * @param agentsToAllocate agents that still haven't been allocated and can be used
	 */
	private void assignBottleneck(ArrayList<Location> bottleneck, ArrayList<Agent> agentsToAllocate, Map map) {
		for(Location b: bottleneck) {
			Collections.sort(agentsToAllocate, new DistToLocationComparator(map, b)); // can be faster by placing in front of cycle, with a minor loss of accuracy
			Agent a = agentsToAllocate.remove(0);
			a.setTargetLocation(b);
		}
	}

	/**
	 * Get a bottleneck
	 * @param bottleneckPassFreqs
	 * @param f
	 * @return
	 */
	private ArrayList<Location> getBottleNeckOfFreq(HashMap<ArrayList<Location>, Integer> bottleneckPassFreqs, int f) {
		Iterator it = bottleneckPassFreqs.entrySet().iterator();
		while(it.hasNext()) {
			HashMap.Entry pair = (HashMap.Entry) it.next();
			int realF = (int) pair.getValue();
			if (realF > f) {
				return (ArrayList<Location>) pair.getKey();
			}
		}
		return null;
	}


	private boolean hasCommonElements(ArrayList<Location> list1, ArrayList<Location> list2) {
		for (Location t1: list1) {
			for (Location t2: list2) {
				if (t1.equals(t2)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * find bottleneck of a specified width
	 * @param width - specified width
	 * @return - list of bottlenecks of the specified width
	 */
	private ArrayList<ArrayList<Location>> findBottlenecks(int width) {
		ArrayList<ArrayList<Location>> bottleneckList = new ArrayList<ArrayList<Location>>();
		// check rows
		for (int i = 0; i < map.getHeight(); i++) {
			for (int j = 1; j < map.getWidth()-width; j++) {
				if(isEmptyWinRow(i, j, width) && map.getLocation(i, j - 1).isObstacle() && map.getLocation(i, j + width).isObstacle()) { // check for an empty sequennce of locations and whether it is surrounded by obstacles
					ArrayList<Location> bneck = new ArrayList<Location>();
					for (int k = 0; k < width; k++) {
						bneck.add(map.getLocation(i, j + k));
					}
					bottleneckList.add(bneck);
				}
			}
		}
		// check columns
		for (int j = 0; j < map.getWidth(); j++) {
			for (int i = 1; i < map.getHeight()-width; i++) {

				if(isEmptyWinCol(j, i, width) && map.getLocation(i - 1, j).isObstacle() && map.getLocation(i + width, j).isObstacle()) { // check for an empty sequennce of locations and whether it is surrounded by obstacles
					ArrayList<Location> bneck = new ArrayList<Location>();
					for (int k = 0; k < width; k++) {
						bneck.add(map.getLocation(i + k, j));
					}
					bottleneckList.add(bneck);
				}
			}
		}
		return bottleneckList;
	}


	private boolean isEmptyWinRow(int row, int j, int size) {
		for (int k = 0; k < size; k++) {
			if (map.getLocation(row, j + k).isObstacle()) {
				return false;
			}
		}
		return true;
	}

	private boolean isEmptyWinCol(int col, int j, int size) {
		for (int k = 0; k < size; k++) {
			if (map.getLocation(j + k, col).isObstacle()) {
				return false;
			}
		}
		return true;
	}
	

	public void reNew() {
		super.reNew();
		bottlenecks = null;
	}

}
