
import java.util.ArrayList;

public class RandomOrderGreedyStrategy extends Strategy {


	
	public RandomOrderGreedyStrategy() {
		super();
	}
	
	/**
	 * Allocate the targets to the defensive agents as follows:
	 * for each agent in the order as the iterator returns
	 * 		find the closest target from the list of available targets
	 * 		assign the target to the agent and remove the target from the list of targets
	 * 
	 * TODO: if there is more defensive agents than targets, we should still assign something. 
	 * 		Right now it would return null.
	 * @param targets
	 */
	@Override
	public void allocateTargets(Map map, Team defTeam, Team ofTeam, boolean reallocate, int considerAgents) {
		targets = map.getTargets();
		ArrayList<Location> availableTargets = new ArrayList<Location>(targets); 
		for (Agent agent: defTeam) {
			if (availableTargets== null || availableTargets.size() == 0) {
				System.err.println("No more available targets");
				System.exit(1);
			}
			if (agent.getTargetLocation() == null) { // allocate agent only if he hasn't any target yet
				Location myNewTarget = agent.getClosestLocation(availableTargets);
				agent.setTargetLocation(myNewTarget);
				availableTargets.remove(myNewTarget);
			}
		}	
	}

	public String toString() {
		return "RND GREEDY STRATEGY";
	}

}
