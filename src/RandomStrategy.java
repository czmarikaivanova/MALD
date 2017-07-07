import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class RandomStrategy extends Strategy {

	private Random randomGen;
	private ArrayList<Location> targets;
	
	public RandomStrategy() {
		super();
		randomGen = new Random(10);
	}
	
	/**
	 * allocate targets to agents randomly
	 * @param targets
	 */
	@Override
	public void allocateTargets(Map map, Team team, boolean reallocate, int considerAgents) {
		targets = map.getTargets();
		Collections.shuffle(targets, randomGen);	
		int i = 0;
		for (Agent agent: team) {
			if (i >= targets.size()) {  // assign always only one target to one agent
				return;
			}
			if (agent.getTargetLocation() == null) { // allocate agent only if he hasn't any target yet
				agent.setTargetLocation(targets.get(i));
				i++;
			}
		}		
	}
	
	public String toString() {
		return "RANDOM STRATEGY";
	}



}
