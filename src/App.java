import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class App {
	private Map map;
	private Team offAgents;
	private Team defAgents;
	private int maxMoves = 100;
	private ArrayList<Strategy> strategies;
	
	public App(File input) {
		initialize(input); // this initialization is only for the bottleneck calculation. Otherwise it we initialize the map before every strategy starts
		strategies = new ArrayList<Strategy>();
//		strategies.add(new RandomStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new RandomStrategy(false, false, Constants.CONSIDER_AGENTS_ALL));
//		strategies.add(new RandomOrderGreedyStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new RandomOrderGreedyStrategy(false, false, Constants.CONSIDER_AGENTS_ALL));
//		strategies.add(new RandomOrderGreedyStrategy(false, false, Constants.CONSIDER_AGENTS_ALL));
//		strategies.add(new RandomStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new Greedy2Strategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new BottleneckStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new BottleneckImprovedStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new BottleneckImprovedStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new PathFreqStrategy(false, false, Constants.CONSIDER_AGENTS_NONE, false));
		strategies.add(new PathFreqStrategy(false, false, Constants.CONSIDER_AGENTS_NONE, true));

		//		strategies.add(new BottleneckStrategy(false, false, Constants.CONSIDER_AGENTS_OPPONENT));
		int[][] resArray = new int[maxMoves][strategies.size()];
		
		for (Strategy s: strategies) {
			System.out.println("Starting strategy " + s.toString());
			initialize(input);
			printState();
			int moveCnt = 0;
			while (!offAgents.finished() && moveCnt < maxMoves) {
				offAgents.playMove(map);
				s.allocateTargets(map, defAgents, offAgents);
	//			defAgents.allocateTargetsBottlenecks(bottlenecks, false, Constants.CONSIDER_AGENTS_NONE);  // scecond parameter true if we want to reallocate agents that have reached their targets
				defAgents.playMove(map);
				printState();
				resArray[moveCnt][strategies.indexOf(s)] = offAgents.finishedCnt();
				moveCnt++;
			}
			
		}
		printState();
		writeResultsToFile(strategies, resArray);
		int finishedCnt = offAgents.finishedCnt();
		System.out.println("### " + finishedCnt + "/" + offAgents.agentCnt() + " agents reached their destination");
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


	
	private void writeResultsToFile(ArrayList<Strategy> strategies, int[][] resArray) {
		File f = new File("output/output" + new File("output/").listFiles().length + ".data" );
		try {
			f.createNewFile();
			Writer output = new BufferedWriter(new FileWriter(f, true));
			String firstLine = "tms \t ";
			for (Strategy s: strategies) {
				firstLine += "\"" + s.toString() + "\"" + "\t";
			}
			firstLine += "\n";
			output.write(firstLine);
			for (int i = 0; i < resArray.length; i++) {
				output.write(i + "\t");
				for (int j = 0; j < resArray[i].length; j++) {
					output.write("\t" + resArray[i][j]);
				}
				output.write("\n");
			}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
