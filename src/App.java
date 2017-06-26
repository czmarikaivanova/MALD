import java.io.File;
import java.util.ArrayList;

public class App {
	private Map map;
	private Team offAgents;
	private Team defAgents;
	private int maxMoves = 50;
	
	public App(File input) {
		initialize(input);
		printState();
		int moveCnt = 0;
		while (!offAgents.finished() && moveCnt < maxMoves) {
			for (Agent a: offAgents) {
				Location next = a.BFS(map, a.getTargetLocation());
				a.makeMove(map, next);
			}
			moveCnt++;
			printState();
		}
	}

	private void printState() {
		System.out.println(map.toString());
		System.out.println(offAgents.toString());
		System.out.println(defAgents.toString());
	}

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
