import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Random;

public class Main {
	
	static int agentCnt;
	static int createdAgents;
	private static FileInputStream fileInputStream;
	private static FileOutputStream fileOutputStream;
	private static Random rndGen;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		createdAgents = 0;
		File input = new File("maps/isound1.map");
		File input_new = new File("maps/isound1_new.map");
		try {
			copyFileUsingChannel(input, input_new);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int offCnt = 40;
		int defCnt = 30;
		agentCnt = offCnt + defCnt;
		int x1 = 2;
		int y1 = 33;
		int w1 = 10;
		int h1 = 10;
		int x2 = 6;
		int y2 = 3;
		int w2 = 45;
		int h2 = 20;
		generateOffensive(x1, y1, w1, h1, x2, y2, w2, h2, offCnt, input_new);
		int x = 35;
		int y = 35;
		int w = 10;
		int h = 10;
		generateDeffensive(x,y,w,h,defCnt,input_new);
		new App(input_new);	
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
		ArrayList<Pair<Integer, Integer>> agentCoords = new ArrayList<Pair<Integer,Integer>>();
		rndGen = new Random();
		for (int i = 0; i < defCnt; i ++) {
			boolean hitEmpty = false;
			int r1 = 0;
			int r2 = 0;
			Pair<Integer, Integer> rpair = null;
			while (!hitEmpty) {
				r1 = rndGen.nextInt(w);
				r2 = rndGen.nextInt(h);
				rpair = new Pair<Integer, Integer>(r1, r2);
				if (!containsPair(agentCoords, rpair)) {
					hitEmpty = true;
				}
			}
			agentCoords.add(rpair);
			int ax = x + r1;
			int ay = y + r2;
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

		ArrayList<Pair<Integer, Integer>> agentCoords = new ArrayList<Pair<Integer,Integer>>();
		ArrayList<Pair<Integer, Integer>> targetCoords = new ArrayList<Pair<Integer,Integer>>(); 
		for (int i = 0; i < offCnt; i ++) {
			boolean hitEmpty = false;
			int r1 = 0;
			int r2 = 0;
			int t1 = 0;
			int t2 = 0;
			Pair<Integer, Integer> rpair = null;
			while (!hitEmpty) {
				r1 = rndGen.nextInt(w1);
				r2 = rndGen.nextInt(h1);
				rpair = new Pair<Integer, Integer>(r1, r2);
				if (!containsPair(agentCoords, rpair)) {
					hitEmpty = true;
				}
			}
			agentCoords.add(rpair);
			boolean hitEmpty2 = false;

			Pair<Integer, Integer> tpair = null;
			while (!hitEmpty2) {
				t1 = rndGen.nextInt(w2);
				t2 = rndGen.nextInt(h2);
				tpair = new Pair<Integer, Integer>(t1, t2);
				if (!containsPair(targetCoords, tpair)) {
					hitEmpty2 = true;
				}
			}
			targetCoords.add(tpair);
			int ax = x1 + r1;
			int ay = y1 + r2;
			int tx = x2 + t1;
			int ty = y2 + t2;
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
