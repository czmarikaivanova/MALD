
public abstract class Strategy {
	// assign targets according to a certain strategy.
	public abstract void allocateTargets( Map map, Team team, boolean reallocate, int considerAgents);
	
}
