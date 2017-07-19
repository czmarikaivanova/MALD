import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

public class Main {
	
	static int agentCnt;
	static int createdAgents;
	private static FileInputStream fileInputStream;
	private static FileOutputStream fileOutputStream;
	private static Random rndGen;
	private static int maxIter = 2;
	private static int maxMoves = 150;
	
	private static ArrayList<Pair<Integer, Integer>> agentCoords;
	private static ArrayList<Pair<Integer, Integer>> targetCoords; 
	private static ArrayList<Pair<Integer, Integer>> obstacleCoords; 
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ArrayList<Strategy> strategies = new ArrayList<Strategy>();
		strategies.add(new RandomStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new RandomStrategy(false, false, Constants.CONSIDER_AGENTS_ALL));
		strategies.add(new RandomOrderGreedyStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new RandomOrderGreedyStrategy(false, false, Constants.CONSIDER_AGENTS_ALL));
//		strategies.add(new RandomOrderGreedyStrategy(false, false, Constants.CONSIDER_AGENTS_ALL));
//		strategies.add(new RandomStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new Greedy2Strategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new BottleneckStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
		strategies.add(new BottleneckImprovedStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new BottleneckImprovedStrategy(false, false, Constants.CONSIDER_AGENTS_NONE));
//		strategies.add(new PathFreqStrategy(false, false, Constants.CONSIDER_AGENTS_NONE, false, true));
		strategies.add(new PathFreqStrategy(false, false, Constants.CONSIDER_AGENTS_NONE, true, false));

		//		strategies.add(new BottleneckStrategy(false, false, Constants.CONSIDER_AGENTS_OPPONENT));
		int[][] resArray = new int[maxMoves][strategies.size() * maxIter];
		
		for (int iter = 0; iter < maxIter; iter++) {
			createdAgents = 0;
			File input = new File("maps/rooms1.map");
			File input_new = new File("maps/rooms1_new.map");
			try {
				copyFileUsingChannel(input, input_new);
			} catch (IOException e) {
				e.printStackTrace();
			}
			agentCoords = new ArrayList<Pair<Integer,Integer>>();
			targetCoords = new ArrayList<Pair<Integer,Integer>>(); 
			obstacleCoords = new ArrayList<Pair<Integer,Integer>>(); 
			readObstacleCoords(input_new);
			int offCnt = 60;
			int defCnt = 30;
			agentCnt = offCnt + defCnt;
			int x1 = 1;
			int y1 = 1;
			int w1 = 15;
			int h1 = 15;
			int x2 = 45;
			int y2 = 55;
			int w2 = 15;
			int h2 = 15;
			int seed = 1 ;
			rndGen = new Random(seed);
			System.out.println(seed);
			generateOffensive(x1, y1, w1, h1, x2, y2, w2, h2, offCnt, input_new);
			int x = 1;
			int y = 1;
			int w = 15;
			int h = 15;
			generateDeffensive(x,y,w,h,defCnt,input_new);
			
	//		 offCnt = 60;
	//		 defCnt = 30;
	//		agentCnt = offCnt + defCnt;
	//		 x1 = 2;
	//		 y1 = 2;
	//		 w1 = 20;
	//		 h1 = 20;
	//		 x2 = 33;
	//		 y2 = 33;
	//		 w2 = 20;
	//		 h2 = 20;
	//		rndGen = new Random(1234567);
	//		generateOffensive(x1, y1, w1, h1, x2, y2, w2, h2, offCnt, input_new);
	//		 x = 2;
	//		 y = 2;
	//		 w = 20;
	//		 h = 20;
	//		generateDeffensive(x,y,w,h,defCnt,input_new);
			new App(input_new, strategies, resArray, maxMoves, iter);	
		}
	}

	private static void readObstacleCoords(File input_new) {
		try (BufferedReader br = new BufferedReader(new FileReader(input_new))) {
			boolean mapStarted = false;
			int linCnt = 0;
		    String line;
		    while ((line = br.readLine()) != null) {
		    	if (line.equalsIgnoreCase("map")) {
		    		mapStarted = true;
		    		line = br.readLine();
			    	linCnt++;
		    	}
		    	if (mapStarted) {
			    	int charCnt = 0;
			    	for (Character c : line.toCharArray()) {
			    		if (!c.equals(Constants.TERRAIN_CHAR)) {
			    			obstacleCoords.add(new Pair<>(linCnt - 4, charCnt));
			    		}
			    		charCnt++;
			    	}
		    	}
		    	linCnt++;
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * generate offensive agents of specified number in a rectangle with specified placement and size
	 * @param x x-coordinate of the top-left corner
	 * @param y y-coordinate of the top-left corner
	 * @param w width of the rectangle
	 * @param h height of the rectangle
	 * @param defCnt number of defending agent
	 * @param input map
	 */
	private static void generateDeffensive(int x, int y, int w, int h, int defCnt, File input) {
		if (w*h < defCnt ) {
			System.err.println("insufficient space for generating agents");
			System.exit(1);
		}
		try {
		Writer output;
		output = new BufferedWriter(new FileWriter(input, true));  //clears file every time
//		ArrayList<Pair<Integer, Integer>> agentCoords = new ArrayList<Pair<Integer,Integer>>();
		int ax = 0;
		int ay = 0;
		for (int i = 0; i < defCnt; i ++) {
			boolean hitEmpty = false;
			int r1 = 0;
			int r2 = 0;

			Pair<Integer, Integer> rpair = null;
			while (!hitEmpty ) {
				r1 = rndGen.nextInt(w);
				r2 = rndGen.nextInt(h);
				ax = x + r1;
				ay = y + r2;
				rpair = new Pair<Integer, Integer>(ax, ay);
				if (!containsPair(agentCoords, rpair) && !containsPair(obstacleCoords, rpair)) {
					hitEmpty = true;
				}
			}
			agentCoords.add(rpair);
			output.append(createdAgents + "," + ax + "," + ay + "\n");
			createdAgents++;
		}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param x1
	 * @param y1
	 * @param w1
	 * @param h1
	 * @param x22
	 * @param y2
	 * @param w2
	 * @param h2
	 * @param offCnt
	 * @param input
	 */
	private static void generateOffensive(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2, int offCnt,
			File input) {
		if (w1*h1 < offCnt || w2 * h2 < offCnt) {
			System.err.println("insufficient space for generating agents");
			System.exit(1);
		}
		try {
		Writer output;
		output = new BufferedWriter(new FileWriter(input, true));  //clears file every time
		output.append("Agents: \n");
		output.append(agentCnt + "\n");


		for (int i = 0; i < offCnt; i ++) {
			boolean hitEmpty = false;
			int r1 = 0;
			int r2 = 0;
			int t1 = 0;
			int t2 = 0;
			int ax = 0;
			int ay = 0;
			Pair<Integer, Integer> rpair = null;
			while (!hitEmpty) {
				r1 = rndGen.nextInt(w1);
				r2 = rndGen.nextInt(h1);
				ax = x1 + r1;
				ay = y1 + r2;
				rpair = new Pair<Integer, Integer>(ax, ay);
				if (!containsPair(agentCoords, rpair) && !containsPair(obstacleCoords, rpair)) {
					hitEmpty = true;
				}
			}
			agentCoords.add(rpair);
			boolean hitEmpty2 = false;

			int tx = 0;
			int ty = 0;
			Pair<Integer, Integer> tpair = null;
			while (!hitEmpty2) {
				t1 = rndGen.nextInt(w2);
				t2 = rndGen.nextInt(h2);
				tx = x2 + t1;
				ty = y2 + t2;
				tpair = new Pair<Integer, Integer>(tx, ty);
				if (!containsPair(targetCoords, tpair)  && !containsPair(obstacleCoords, tpair)) {
					hitEmpty2 = true;
				}
			}
			targetCoords.add(tpair);
			output.append(createdAgents + "," + ax + "," + ay + "," + tx + "," + ty + "\n");
			createdAgents++;
		}
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * check whether a list of Pairs contains an element with a corresponding content
	 * @param list of pairs
	 * @param p pair we are looking for
	 * @return
	 */
	private static boolean containsPair(ArrayList<Pair<Integer, Integer>> list, Pair<Integer, Integer> p) {
		for (Pair<Integer, Integer> pair: list) {
			if (p.equals(pair)) {
				return true;
			}
		}
		return false;
	}
	
	private static void copyFileUsingChannel(File source, File dest) throws IOException {
	    FileChannel sourceChannel = null;
	    FileChannel destChannel = null;
	    try {
	        fileInputStream = new FileInputStream(source);
			sourceChannel = fileInputStream.getChannel();
	        fileOutputStream = new FileOutputStream(dest);
			destChannel = fileOutputStream.getChannel();
	        destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
	       }finally{
	           sourceChannel.close();
	           destChannel.close();
	   }
	}
}
