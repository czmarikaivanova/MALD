
import java.util.ArrayList;

public abstract class Strategy {
	
	protected ArrayList<Location> targets;
	protected boolean multiStage;
	protected boolean firstRun;
	
	public Strategy(boolean multiStage) {
		this.multiStage = multiStage;
	}
	
	// assign targets according to a certain strategy.
	public abstract void allocateTargets( Map map, Team defTeam, Team offTeam, boolean reallocate, int considerAgents);
	
}
