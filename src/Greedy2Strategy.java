import java.util.ArrayList;

public class Greedy2Strategy extends Strategy {
	
	public Greedy2Strategy(boolean multiStage, boolean reallocate, int considerAgents) {
		super(multiStage, reallocate, considerAgents);
	}
	
	@Override
	protected void allocateTargets() {
		BFS bfs = new BFS(considerAgents);
		ArrayList<Location> availableTargets = new ArrayList<Location>(targets);
		ArrayList<Agent> availableAgents = new ArrayList<Agent>();
		for (Agent a: defTeam) {
			availableAgents.add(a);
		}
		while (!availableTargets.isEmpty() && !availableAgents.isEmpty()) {
			int minDst = Constants.INFINITY;
			Agent minAgent = null;
			Location minTarget = null;
			for (Agent a: availableAgents) {
				Location target = a.getClosestLocation(availableTargets, considerAgents);
				int dst = bfs.minPathLength(a.getCurrentLocation(), target, map);
				if (dst < minDst) {
					minDst = dst;
					minAgent = a;
					minTarget = target;
				}
			}
			availableTargets.remove(minTarget);
			availableAgents.remove(minAgent);
			minAgent.setTargetLocation(minTarget);
		}
		System.out.println("fed");
	}

}
