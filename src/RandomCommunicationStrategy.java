import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

public class RandomCommunicationStrategy extends Strategy {

	private Random randomGen;
	
	public RandomCommunicationStrategy(boolean multiStage, boolean reallocate, int considerAgents) {
		super(multiStage, reallocate, considerAgents);
		randomGen = new Random(10);
	}
	@Override
	protected void allocateTargets() {
		ArrayList<Location> availableTargets = new ArrayList<Location>(targets);  // a separate list of targets
		defTeam.shuffle();
		Pair<ArrayList<Agent>, ArrayList<Agent>> dividedDefenders = divideAgentsIntoDefAndComcomCnt();
		ArrayList<Agent> realDefendersAvailable = dividedDefenders.getFirst();
		ArrayList<Agent> communicators = dividedDefenders.getSecond();
		PriorityQueue<ArrayList<Location>> ccs = determineConComps(availableTargets);
		while (!ccs.isEmpty()) { // iterate over all connected components
			ArrayList<Location> cc = ccs.poll(); // remove the biggest CC from the list of CCs
			int maxAssignments = Math.min(cc.size(), realDefendersAvailable.size()); // determine number of assignments that will take place for this cc (either we have enough agents, then all targets from cc will be assigned, or only as many as we have available agantes
			for (int i = 0; i < maxAssignments; i++) {
				Agent agentToAllocate = realDefendersAvailable.remove(0); // remove the first agent from the agents available for allocation 
				Location myNewTarget = cc.get(randomGen.nextInt(cc.size())); // find the closest location from the current CC. This is the 'random' step 
				agentToAllocate.setTargetLocation(myNewTarget); // set the closest location as the agent's new target
				cc.remove(myNewTarget); // remove this location from the CC, because it should not be assigned again.
			}
		}
		communicators.addAll(realDefendersAvailable); // this should not happen. The realDefenders should be empty
		// now all normal defenders are allocated. We should now compute the connected components and allocate
		// remaining defenders to the positions suitable for communication
		allocateCommunicators(communicators);
		
	}

	public String toString() {
		return "RANDOM STRATEGY COMMUNICATION" + considerAgents;
	}

	
}
