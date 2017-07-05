import java.io.File;

public class App {
	private Map map;
	private Team offAgents;
	private Team defAgents;
	private int maxMoves = 100;
	private int sort = 0;
	
	public App(File input) {
		initialize(input);
		int moveCnt = 0;
		defAgents.allocateTargetsRandom();
//		defAgents.allocateTargetsRndOrderGreedy();
		defAgents.allocateTargetsBottlenecks();
		printState();
		System.exit(0);
		while (!offAgents.finished() && moveCnt < maxMoves) {
			offAgents.playMove(map);
			defAgents.playMove(map);
			printState();
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
//		System.out.println(offAgents.toString());
//		System.out.println(defAgents.toString());
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
		offAgents = new Team(Constants.OFFENSIVE_TEAM, map, null);
		defAgents = new Team(Constants.DEFENSIVE_TEAM, map, offAgents);
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
