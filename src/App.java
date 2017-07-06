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
	
	public App(File input) {
		initialize(input);
		File outputFile = initOutputFile();
		int moveCnt = 0;
//		defAgents.allocateTargetsRandom();
//		defAgents.allocateTargetsRndOrderGreedy();
		ArrayList<ArrayList<Location>> bottlenecks = findBottlenecks(3);
		printState();
		System.exit(0);
		while (!offAgents.finished() && moveCnt < maxMoves) {
			
			offAgents.playMove(map);
			defAgents.allocateTargetsBottlenecks(bottlenecks, false, Constants.CONSIDER_AGENTS_NONE);  // scecond parameter true if we want to reallocate agents that have reached their targets
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

	/**
	 * find bottleneck of a specified width
	 * @param width - specified width
	 * @return - list of bottlenecks of the specified width
	 */
	private ArrayList<ArrayList<Location>> findBottlenecks(int width) {
		ArrayList<ArrayList<Location>> bottleneckList = new ArrayList<ArrayList<Location>>();
		// check rows
		for (int i = 0; i < map.getHeight(); i++) {
			for (int j = 1; j < map.getWidth()-width; j++) {
				if(isEmptyWinRow(i, j, width) && map.getLocation(i, j - 1).isObstacle() && map.getLocation(i, j + width).isObstacle()) { // check for an empty sequennce of locations and whether it is surrounded by obstacles
					ArrayList<Location> bneck = new ArrayList<Location>();
					for (int k = 0; k < width; k++) {
						bneck.add(map.getLocation(i, j + k));
					}
					bottleneckList.add(bneck);
				}
			}
		}
		// check columns
		for (int j = 0; j < map.getWidth(); j++) {
			for (int i = 1; i < map.getHeight()-width; i++) {

				if(isEmptyWinCol(j, i, width) && map.getLocation(i - 1, j).isObstacle() && map.getLocation(i + width, j).isObstacle()) { // check for an empty sequennce of locations and whether it is surrounded by obstacles
					ArrayList<Location> bneck = new ArrayList<Location>();
					for (int k = 0; k < width; k++) {
						bneck.add(map.getLocation(i + k, j));
					}
					bottleneckList.add(bneck);
				}
			}
		}
		return bottleneckList;
	}


	private boolean isEmptyWinRow(int row, int j, int size) {
		for (int k = 0; k < size; k++) {
			if (map.getLocation(row, j + k).isObstacle()) {
				return false;
			}
		}
		return true;
	}

	private boolean isEmptyWinCol(int col, int j, int size) {
		for (int k = 0; k < size; k++) {
			if (map.getLocation(j + k, col).isObstacle()) {
				return false;
			}
		}
		return true;
	}
	
	private File initOutputFile() {
		File f = new File("output/output" + new File("output/").listFiles().length + ".txt" );
		
		try {
			f.createNewFile();
			Writer output = new BufferedWriter(new FileWriter(f, true));
			output.write("# tms \t at_target \t ");
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f; 
	}
	
	private void updateOutput(File f) {
		
	}
}
