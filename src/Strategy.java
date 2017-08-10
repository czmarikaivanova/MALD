
import java.util.ArrayList;
import java.util.LinkedList;

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
	
	/**
	 * randomly assign targets to offensive agents and calculate shortest paths
	 * @return
	 */
	protected ArrayList<ArrayList<Location>> estimatePaths(Map map, ArrayList<Location> forbidden) {
		ArrayList<ArrayList<Location>> paths = new ArrayList<ArrayList<Location>>();
		ArrayList<Location> targets = map.getTargets();
		int i = 0;
		for (Agent a : offTeam) {
			Location t = targets.get(i);  // guess a target by id
			i++;
			LinkedList<Location> path = new BFS(considerAgents).findPathWithout(a.getCurrentLocation(), t, forbidden, map); 
			paths.add(new ArrayList<Location>(path));
		}
		return paths;
	}
	
	
	protected abstract void allocateTargets();

	public void reNew() {
		firstRun = true;
		
	}
	
	protected boolean sameLengts(ArrayList<ArrayList<Location>> paths, ArrayList<ArrayList<Location>> updatedPaths) {
		for (int i = 0; i < paths.size(); i++) {
			if (paths.get(i).size() != updatedPaths.get(i).size()) {
				return false;
			}
		}
		return true;
	}
	
}
