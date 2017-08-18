import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Map implements Iterable<Location> {

	private static final int maxVisDst = 10;
	
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document doc;
	private NodeList nList;
	private Location[][] grid;
	private boolean[][] visGraph;
	private int[][] dstGraph;
	private ArrayList<Location> targets;
//	private ArrayList<Location> nonObstLocatoins;
	private int width;
	private int height;
	private int locationCount;
	Random rndGen;
	Team defenders;


	public Map(File input, boolean shouldCreateVisMap) {
		super();
		rndGen = new Random(1);
		String extension = "";
		int i = input.getName().lastIndexOf('.');
		if (i > 0) {
		    extension = input.getName().substring(i+1);
		}
		if (extension.equalsIgnoreCase("xml")) { // input is a xml file
			dbFactory = DocumentBuilderFactory.newInstance();
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(input);
				doc.getDocumentElement().normalize();
				createMapFromXML();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else { // input is a map
			targets = new ArrayList<Location>();
			readMap(input);
		}
		locationCount = width * height;
		if (shouldCreateVisMap) {
			createDistanceGraph();
			createVisibilityGraph();
		}
	}

	/**
	 * fill the boolean 2D array of visibility 
	 */
	private void createVisibilityGraph() {
		int noObstCnt = setLinIds();
		visGraph = new boolean[noObstCnt][noObstCnt];
		for (Location loc1: this) {
			for (Location loc2: this) {
				if (loc1 != loc2 && !loc1.isObstacle() && !loc2.isObstacle() && dstGraph[loc1.getLinId()][loc2.getLinId()] <= maxVisDst) {
					Line line = new Line(loc1, loc2);
					visGraph[loc1.getLinId()][loc2.getLinId()] = !line.hasObstacles(this);
				}
			}
		}
	}

	/**
	 *
	 * @param l1
	 * @param l2
	 * @return return a distance between two locations 
	 */
	public int getDst(Location l1, Location l2) {
		if (l1.isObstacle() || l2.isObstacle()) {
			System.err.println("no distance data for obstacles");
		}
		return dstGraph[l1.getLinId()][l2.getLinId()];
	}
	
	
	/**
	 * 
	 * @param id1
	 * @param id2
	 * @return return a distance between two locations given their IDs
	 */
	public int getDst(int id1, int id2) {
		Location l1 = getLocationById(id1);
		Location l2 = getLocationById(id2);
		if (l1.isObstacle() || l2.isObstacle()) {
			System.err.println("no distance data for obstacles");
		}
		return dstGraph[l1.getLinId()][l2.getLinId()];
	}
	
	
	/**
	 * fill the int 2D array of (Manhattan) distances between every two locations 
	 */
	private void createDistanceGraph() {
		int noObstCnt = setLinIds();
		dstGraph = new int[noObstCnt][noObstCnt];
		for (Location loc1: this) {
			if (!loc1.isObstacle()) {
				BFS bfs = new BFS(Constants.CONSIDER_AGENTS_NONE);
				int[] dsts = bfs.distsToLocation(this, loc1);
				int id = 0;
				for (int dst: dsts) {
					if (!this.getLocationById(id).isObstacle()) {
						dstGraph[loc1.getLinId()][this.getLocationById(id).getLinId()] = dst;
					}
					id ++;
				}
			}
		}
	}

	/**
	 * assign a unique linear id for a non-obstacle location and create an array list indexed by lin IDs
	 * 
	 * @return the last id - number of non-obstacle locaitons
	 */
	private int setLinIds() {
//		nonObstLocatoins = new ArrayList<>();
		int id = 0;
		for (Location loc: this) {
			if (!loc.isObstacle()) {
//				nonObstLocatoins.add(loc);
				loc.setLinId(id);
				id++;
			}
		}
		return id;
	}

	
	public int getLocationCount() {
		return locationCount;
	}
	
	public String toString() {
		String mapStr = "";
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				if (grid[h][w].isObstacle()) {
					mapStr += "\u2588" +"|";
				}
				else {
					Agent a = grid[h][w].getAgent();
					if (a != null) {
						mapStr += a.getTeam() + "|";
					} else if (grid[h][w].isSomeDestination()) {
						mapStr += "o|";
					} else {
						mapStr += "_|";						
					}
				}
			}
			mapStr += "\n";
		}
//		mapStr += "\n TARGETS: \n";
//		for (Location t: targets) {
//			mapStr += t.toString() + "\n";
//		}
		return mapStr;
	}
	
	public String toStringVis() {
		String s = "";
		for (int i = 0; i < visGraph.length; i++) {
			for (int j = 0; j < visGraph[i].length; j++) {
				if (visGraph[i][j]) {
					s += "1 ";
				}
				else {
					s += "0 ";
				}
			}
			s += "\n";
		}
		return s;
	}
	
	public String toStringDst() {
		String s = "";
		for (int i = 0; i < dstGraph.length; i++) {
			for (int j = 0; j < dstGraph[i].length; j++) {
				s += String.format("%3d", dstGraph[i][j]);
			}
			s += "\n";
		}
		return s;
	}
	
	/**
	 * return the Location in the map
	 * @param x - coordinate
	 * @param y - coordinate
	 * @return
	 */
	public Location getLocation(int x, int y) {
		return grid[x][y];
	}
	
	public Location getLocationById(int id) {
		int x = id / width;
		int y = id % width;
		return grid[x][y];
	}
	
	public ArrayList<Location> neighbors(Location loc, boolean onlyEmpty, boolean diag, boolean mustBeConnected) {
		ArrayList<Location> neighbours = new ArrayList<Location>();
		for (int dir1 = -1; dir1 <= 1; dir1++) {
			for (int dir2 = -1; dir2 <= 1; dir2++) {
				if (dir1 != 0 || dir2 != 0) {
					if ((Math.abs(dir1) != Math.abs(dir2)) || diag) { // do not include the current node in BFS and do not consider diagonals
						if (loc == null) {
							System.out.println("loc null");
						}
						int x = loc.getX() + dir1;
						int y = loc.getY() + dir2;
						if (x >= 0 && x < height && y >= 0 && y < width) { // we should not get out of the map
							Location adjLoc = getLocation(x, y);
							if (!adjLoc.isObstacle() && (!onlyEmpty || (adjLoc.getAgent() == null))) { // it will not be null
								if (!mustBeConnected || isConnected(loc, adjLoc)) {
									neighbours.add(adjLoc);
								}
							}
						}
					}
				}
			}
		}
//		Collections.shuffle(neighbours, new Random(1));
		Collections.shuffle(neighbours, rndGen);
		return neighbours;
	}

	/**
	 * Using DFS check whether the induced visibility subgraph is connected
	 * @param loc the original location
	 * @param adjLoc the neighbour for whcih we want to check whether the connectivity is preserved
	 * @return
	 */
	public boolean isConnected(Location loc, Location adjLoc) {
		ArrayList<Location> locstoProcess = new ArrayList<Location>();
		ArrayList<Location> locsThatShouldBeConnected= new ArrayList<Location>();
		ArrayList<Location> visitedLocs = new ArrayList<>();
		locstoProcess.add(adjLoc);
		for (Agent a: defenders) {
			if (a.getCurrentLocation() != loc) { // we don't want to connect with loc, because it is the place that we are leaving
				locstoProcess.add(a.getCurrentLocation());
			}
		}
		locsThatShouldBeConnected.addAll(locstoProcess);
		Stack<Location> stack = new Stack<Location>();
		stack.push(adjLoc);
		while (!stack.empty()) {
			Location locFromStack = stack.pop();
			if (!visitedLocs.contains(locFromStack)) {
				visitedLocs.add(locFromStack);
				locstoProcess.remove(locFromStack);
				ArrayList<Location> visNeighbours = getVisNeighbours(locFromStack, locsThatShouldBeConnected);
				for (Location vNeigh: visNeighbours) {
					if (!visitedLocs.contains(vNeigh) && !stack.contains(vNeigh)) {
//						if (locstoProcess.contains(vNeigh)) {
						stack.push(vNeigh);
					}
				}
			}
		}
		return locstoProcess.isEmpty();
	}

	
	/**
	 * for a given location give all the visible locations
	 * There are many visible locations, but we usually want only those who are e.g. occupied by an agent or those that are targets.
	 * That's why we have the parameter locsThatShouldBeConnected
	 * @param locFromStack
	 * @param locsThatShouldBeConnected - set of locations which should be connected. 
	 * @return
	 */
	public ArrayList<Location> getVisNeighbours(Location locFromStack, ArrayList<Location> locsThatShouldBeConnected) {
		ArrayList<Location> visNeighbours = new ArrayList<Location>();
		int stackLocLinId = locFromStack.getLinId();
		for (Location locToCon: locsThatShouldBeConnected) {
			if (visGraph[stackLocLinId][locToCon.getLinId()]) {
				visNeighbours.add(locToCon);
			}
		}
		return visNeighbours;
	}

	/**
	 * Allows iterating over Locations in a map. The 2D array grid is hidden from outside, but we can iterate over it
	 */
	public Iterator<Location> iterator() {
		return new  MapIterator();
	}

	/**
	 * private class implementing the Iterator over map locations
	 * @author marika
	 *
	 */
	private class MapIterator implements Iterator<Location> {
		private int cursor;

		public MapIterator() {
			this.cursor = 0;
		}

		public boolean hasNext() {
			return this.cursor < Map.this.width * Map.this.height;
		}

		public Location next() {
			if(this.hasNext()) {
				Location currLoc = grid[cursor /width][cursor % width]; // integral division
				cursor ++;
				return currLoc;
			}
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * create a graph from selected XML file
	 */
	private void createMapFromXML() {
		//    System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		// create graph (only nodes without edges. 
		// Edges will be added by normalization
		Element gElement = (Element) doc.getElementsByTagName("map").item(0);            
		width = Integer.parseInt(gElement.getAttribute("width"));
		height = Integer.parseInt(gElement.getAttribute("height"));
		targets = new ArrayList<Location>();
		grid = new Location[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				grid[i][j] = new Location(i, j, j * width + i);
			}
		}
		// OBSTACLES
		nList = doc.getElementsByTagName("obstacle");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				int x = Integer.parseInt(eElement.getAttribute("x"));
				int y = Integer.parseInt(eElement.getAttribute("y"));
				grid[x][y].setObstacle();
			}
		}
		// AGENTS
		nList = doc.getElementsByTagName("agent");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				int id = Integer.parseInt(eElement.getAttribute("id"));
				int teamId = Integer.parseInt(eElement.getAttribute("teamid"));
				int xinit = Integer.parseInt(eElement.getAttribute("xinit"));
				int yinit = Integer.parseInt(eElement.getAttribute("yinit"));
				Location initLocation = grid[xinit][yinit];
				if (teamId == Constants.OFFENSIVE_TEAM) {

					String xtargetS = eElement.getAttribute("xtarget");
					String ytargetS = eElement.getAttribute("ytarget");
					if (!xtargetS.isEmpty()) { // targets defined
						int xtarget = Integer.parseInt(xtargetS);
						int ytarget = Integer.parseInt(ytargetS);
						Location targetLocation = grid[xtarget][ytarget];
						grid[xinit][yinit].setAgent(new Agent(id, teamId, initLocation, targetLocation, this));
						targets.add(targetLocation);
					}
					else {
						grid[xinit][yinit].setAgent(new Agent(id, teamId, initLocation, null, this));

					}
				}
				else {
					grid[xinit][yinit].setAgent(new Agent(id, teamId, initLocation, null, this));
				}
			}
		}
		// TARGETS
		nList = doc.getElementsByTagName("target");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				int x = Integer.parseInt(eElement.getAttribute("xtarget"));
				int y = Integer.parseInt(eElement.getAttribute("ytarget"));
				Location target = getLocation(x, y);
				targets.add(target);
			}
		}

	}
	
	/**
	 * Read map from moving ai file format
	 * @param input file
	 */
	private void readMap(File input) {
		try {
			InputStream fis = new FileInputStream(input);
		  	InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				if (line.matches("height.*")) {
					height = Integer.parseInt(line.split(" ")[1]);
				} 
				else if (line.matches("width.*")) {
					width = Integer.parseInt(line.split(" ")[1]);
				}
				else if (line.matches("map.*")) {
					grid = new Location[height][width];
					for (int i = 0; i < height; i++) {
						for (int j = 0; j < width; j++) {
							grid[i][j] = new Location(i, j, i * width + j);
						}
					}					 
					for (int i = 0; i < height; i ++) {  // width and height are switched here, because the map has it like this and I began other way around
						String mapLine = br.readLine();
						for (int j = 0; j < width; j++) {
							if (mapLine.charAt(j) != Constants.TERRAIN_CHAR) {
								grid[(int) (i)][(int) (j)].setObstacle();
							}
						}
					}
				}

				else if (line.matches("Agents.*")) {
					int agentGroupCnt =  Integer.parseInt(br.readLine());
					String agentLine;
					String[] agentLineSplit;
 					for (int i = 0; i < agentGroupCnt; i++) {
						agentLine = br.readLine();
						agentLineSplit = agentLine.split(",");
						int x = Integer.parseInt(agentLineSplit[1]);
						int y = Integer.parseInt(agentLineSplit[2]);
						int agentId = Integer.parseInt(agentLineSplit[0]);
						if (agentLineSplit.length > 3) {// Offensive agents
							Location target = grid[Integer.parseInt(agentLineSplit[3])][Integer.parseInt(agentLineSplit[4])];
							grid[x][y].setAgent(new Agent(agentId, Constants.OFFENSIVE_TEAM, grid[x][y], target, this));
							targets.add(target);
						}
						else { // Defensive agents
							grid[x][y].setAgent(new Agent(agentId, Constants.DEFENSIVE_TEAM, grid[x][y], this));
						}
					}
				}
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @return list of target locations
	 */
	public ArrayList<Location> getTargets() {
		return targets;
	}

	/**
	 * determine whether a location is adjacent to one of other locations in a list
	 * @param loc 
	 * @param locList
	 * @return
	 */
	public boolean adjacent(Location loc, ArrayList<Location> locList) {
		int x = loc.getX();
		int y = loc.getY();
		for (Location adj: locList) {
			int ax = adj.getX();
			int ay = adj.getY();
			if (Math.abs(x - ax) <= 1 && Math.abs(y-ay) <= 1) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * determine whether a location is adjacent to one of other locations in a list
	 * @param loc 
	 * @param locList
	 * @return
	 */
	public boolean adjacent(Location loc1, Location loc2) {
		return (Math.abs(loc1.getX() - loc2.getX()) <= 1 && Math.abs(loc1.getY() - loc2.getY()) <= 1);
	}

	public boolean inMap(int x, int y) {
		if (x < 0 || y < 0) {
			return false;
		}
		if (x >= height || y >= width) {
			return false;
		}
		return true;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setDefenders(Team defAgents) {
		this.defenders = defAgents;
		
	}

	/**
	 * determine whether two locations can 'see' each other. I. e. If they corresponding entry in visibility graph is true
	 * @param ccLoc
	 * @param loc
	 * @return
	 */
	public boolean canSeeEachOther(Location ccLoc, Location loc) {
		return visGraph[ccLoc.getLinId()][loc.getLinId()];
	}


}
