import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class PathFreqCommunicationStrategy extends PathFreqStrategy {
	
	/**
	 * 
	 * @param multiStage shall we calculate targets in every step
	 * @param relocate - shall we reconsider those agents who already reached their destination
	 * @param considerAgents - shell we consider agents as obstacles
	 * @param minCD - shall we want the min cummulative distance
	 */
	public PathFreqCommunicationStrategy(boolean multiStage, boolean relocate, int considerAgents, boolean minCD, boolean useBrush) {
		super(multiStage, relocate, considerAgents, minCD, useBrush);
	}

	@Override
	public void allocateTargets() {
		ArrayList<Location> availableTargets = new ArrayList<Location>(targets);  // a separate list of targets
		defTeam.shuffle();
		Pair<ArrayList<Agent>, ArrayList<Agent>> dividedDefenders = divideAgentsIntoDefAndComcomCnt();
		ArrayList<Agent> realDefendersAvailable = dividedDefenders.getFirst();
		ArrayList<Agent> communicators = dividedDefenders.getSecond();

		ArrayList<Location> forbidden = new ArrayList<Location>();
		ArrayList<Location> freqLocs = new ArrayList<Location>();
		Location mostFreqLoc;
		// in this loop we always predict paths for all offensive agents and calculate the most frequent location with lowest cumulative distance
		// Centered in this location, we are expanding a square and trying to identify a bottleneck. If we find it, we 
		// assign defensive agents to the locations of the bottleneck and insert these locations to the forbidden list.
		// we hope that it should plug the bottleneck. Then we continue predicting the paths with the plugged bottleneck, and get new prediction
		// and possibly new bottleneck...
		do  {
			if (realDefendersAvailable.isEmpty()) {
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
						assignLocations(bneck, realDefendersAvailable, map);	
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
		if (!realDefendersAvailable.isEmpty()) {
			communicators.addAll(realDefendersAvailable);
		}
		allocateCommunicators(communicators);
		

	}

}
