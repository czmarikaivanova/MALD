import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Map implements Iterable<Location> {

	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document doc;
	private NodeList nList;

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private Location[][] grid;
	private ArrayList<Location> targets;
	private int width;
	private int height;
	private int locationCount;


	public Map(File input) {
		super();
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
			readMap(input);
		}
		locationCount = width * height;
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
					} else {
						mapStr += "_|";						
					}
				}
			}
			mapStr += "\n";
		}
		return mapStr;
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
	
	public ArrayList<Location> neighbors(Location loc, boolean onlyEmpty) {
		ArrayList<Location> neighbours = new ArrayList<Location>();
		for (int dir1 = -1; dir1 <= 1; dir1++) {
			for (int dir2 = -1; dir2 <= 1; dir2++) {
				if (Math.abs(dir1) != Math.abs(dir2)) { // do not include the current node in BFS and do not consider diagonals
					int x = loc.getX() + dir1;
					int y = loc.getY() + dir2;
					if (x >= 0 && x < width && y >= 0 && y < height) { // we should not get out of the map
						Location adjLoc = getLocation(x, y);
						if (!adjLoc.isObstacle() && (!onlyEmpty || (adjLoc.getAgent() == null))) { // it will not be null
							neighbours.add(adjLoc);
						}
					}
				}
			}
		}
		return neighbours;
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
								grid[i][j].setObstacle();
							}
						}
					}
				}
				else if (line.matches("Agents.*")) {
					targets = new ArrayList<Location>();
					int agentCnt = Integer.parseInt(br.readLine());
					String agentLine;
					String[] agentLineSplit;
 					for (int i = 0; i < agentCnt; i++) {
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


}
