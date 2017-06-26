import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
				grid[i][j] = new Location(i, j);
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
