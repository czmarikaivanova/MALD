import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	private int[][] distances;
	private int width;
	private int height;



	public Map(File input) {
		super();
			dbFactory = DocumentBuilderFactory.newInstance();
			try {
				dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(input);
				doc.getDocumentElement().normalize();
				createMapFromXML();
				calculateDistances();
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
	}

	private void calculateDistances() {
		int size = width * height;
		distances = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (i == j) {
					distances[i][j] = 0;
				}
				else {
					distances[i][j] = Constants.INFINITY;
				}
			}
			
		}
		for (Location loc: this) {
			for (Location loc2: this) {
				if (!loc.isObstacle() && !loc2.isObstacle() && loc.isNeighbour(loc2)) {
					distances[loc.getId()][loc2.getId()] = 1;
					distances[loc2.getId()][loc.getId()] = 1;
				}
			}
		}

		 for (int k = 0; k < size; k++) {
		    for (int i = 0; i < size; i++) {
		       for (int j = 0; j < size; j++){
		          if (distances[i][j] > distances[i][k] + distances[k][j]) { 
		        	  distances[i][j] =  distances[i][k] + distances[k][j];
		          }
		       }
		    }
		 }
	}

	private int getDistance(int l1, int l2) {
		return distances[l1][l2];
	}

	public int getDistance(Location loc1, Location loc2) {
		return distances[loc1.getId()][loc2.getId()];
	}
	
	public String toString() {
		String mapStr = "";
		for (int w = 0; w < width; w++) {
			for (int h = 0; h < width; h++) {
				if (grid[w][h].isObstacle()) {
					mapStr += "X";
				}
				else {
					Agent a = grid[w][h].getAgent();
					if (a != null) {
						mapStr += a.getTeam();
					} else {
						mapStr += "_";						
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
				Location currLoc = grid[cursor % width][cursor /width]; // integral division
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
					int xtarget = Integer.parseInt(eElement.getAttribute("xtarget"));
					int ytarget = Integer.parseInt(eElement.getAttribute("ytarget"));
					Location targetLocation = grid[xtarget][ytarget];
					grid[xinit][yinit].setAgent(new Agent(id, teamId, initLocation, targetLocation));
				}
				else {
					grid[xinit][yinit].setAgent(new Agent(id, teamId, initLocation, null));
				}
			}
		}
	}


}
