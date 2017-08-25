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
	int[][] resArray;
	int maxMoves;
	
	public App(File input, ArrayList<Strategy> strategies, int[][] resArray, int maxMoves, int iter) {
		this.resArray = resArray;
		this.maxMoves = maxMoves;
		initialize(input, false); // this initialization is only for the bottleneck calculation. Otherwise it we initialize the map before every strategy starts
	
		
//		Location l1 = map.getLocation(1, 1);
//		Location l2 = map.getLocation(2, 3);
//		System.out.println("Dst between " + l1.toString() + " and " + l2.toString() + " is " + map.getDst(l1, l2));
//		System.out.println(map.toStringDst());
//		System.out.println(map.toStringVis());
		printState();
//		System.exit(0);
		
		
		for (Strategy s: strategies) {
			System.out.println("Starting strategy " + s.toString());
			s.reNew();
			initialize(input, true);

			int moveCnt = 0;
			while (!offAgents.finished() && moveCnt < maxMoves) {
				offAgents.playMove(map);
				s.allocateTargets(map, defAgents, offAgents);
	//			defAgents.allocateTargetsBottlenecks(bottlenecks, false, Constants.CONSIDER_AGENTS_NONE);  // scecond parameter true if we want to reallocate agents that have reached their targets
				defAgents.playMove(map);
				printState();
				resArray[moveCnt][strategies.size() * iter + strategies.indexOf(s)] = offAgents.finishedCnt();
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
	private void initialize(File input, boolean shouldCreateVisMap) {
		map = new Map(input, shouldCreateVisMap);
		createAgents();
		map.setDefenders(defAgents);
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
		offAgents.calculateCenterPoint();
		defAgents.calculateCenterPoint();
	}


	private void writeResultsToFile(ArrayList<Strategy> strategies, int[][] resArray) {
		File f = new File("output/comm/df-1-5/output" + new File("output/comm/df-1-5/").listFiles().length + ".data" );
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
