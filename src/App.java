import java.io.File;

public class App {
	private Map map;
	private Team offAgents;
	private Team defAgents;
	private int maxMoves = 200;
	
	public App(File input) {
		initialize(input);
		printState();
		int moveCnt = 0;
		defAgents.allocateTargetsRandom(map.getTargets());
		while (!offAgents.finished() && moveCnt < maxMoves) {
			printState();
			offAgents.playMove(map);
			defAgents.playMove(map);
			moveCnt++;
		}
		printState();
		if (offAgents.finished()) {
			System.out.println("All agents reached their destinations");
		}
		else {
			int finishedCnt = offAgents.finishedCnt();
			System.out.println("###" + finishedCnt + " agents reached their destination");
		}

	}

	private void printState() {
		System.out.println(map.toString());
		System.out.println(offAgents.toString());
		System.out.println(defAgents.toString());
	}

	/**
	 * initialize map and agents 
	 * @param input File describing the initial configuration
	 */
	private void initialize(File input) {
		map = new Map(input);
		createAgents();
	}

	private void createAgents() {
		offAgents = new Team(Constants.OFFENSIVE_TEAM);
		defAgents = new Team(Constants.DEFENSIVE_TEAM);
		for (Location loc: map) {
			Agent agent = loc.getAgent();
			if (agent != null) {
				if (agent.getTeam() == Constants.OFFENSIVE_TEAM) {
					offAgents.add(agent);
				}
				else {
					defAgents.add(agent);
				}
			}
		}
	}

	
}
