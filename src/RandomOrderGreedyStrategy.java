import java.util.ArrayList;

public class RandomOrderGreedyStrategy extends Strategy {

	private ArrayList<Location> targets;
	
	public RandomOrderGreedyStrategy() {
		super();
	}
	
	@Override
	public void allocateTargets(Map map, Team team, boolean reallocate, int considerAgents) {
		targets = map.getTargets();
		ArrayList<Location> availableTargets = new ArrayList<Location>(targets); 
		for (Agent agent: team) {
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
