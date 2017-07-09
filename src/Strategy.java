
import java.util.ArrayList;

public abstract class Strategy {
	
	protected ArrayList<Location> targets;
	protected boolean multiStage;
	protected boolean firstRun;
	protected boolean reallocate;
	protected int considerAgents;
	protected Team defTeam;
	protected Team offTeam;
	protected Map map;
	
	public Strategy(boolean multiStage, boolean relocate, int considerAgents) {
		this.multiStage = multiStage;
		this.reallocate = relocate;
		this.considerAgents = considerAgents;
		this.firstRun = true;
	}
	
	// assign targets according to a certain strategy.
	public void allocateTargets( Map map, Team defTeam, Team offTeam) {
		this.defTeam = defTeam;
		this.offTeam = offTeam;
		this.map = map;
		this.targets = map.getTargets();
		if (firstRun || multiStage) {
			firstRun = false;
			allocateTargets();
		}
	}
	
	protected abstract void allocateTargets();
	
}
