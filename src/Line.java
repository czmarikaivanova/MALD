import java.util.ArrayList;
import java.util.Collection;

public class Line {

	private Location l1;
	private Location l2;
	
	private static final double DIFF = 0.5; // distance from center of a location to the grid line
	
	public Line(Location l1, Location l2) {
		this.l1 = l1;
		this.l2 = l2;
	}
	
	/**
	 * determine whether the two endpoints of the line can see each other
	 * @param map
	 * @return
	 */
	public boolean hasObstacles(Map map) {
		ArrayList<Pair<Float, Float>> iSections;
		ArrayList<Float> mxList = calculateMs(l1.getCX(), l2.getCX());
		ArrayList<Float> myList = calculateMs(l1.getCY(), l2.getCY());
		if (isHorizontalOrVertical()) {
			iSections = calculateHVIntersections(mxList, myList);
		}
		else {
			ArrayList<Float> txList = calculateTList(mxList, l1.getCX(), l2.getCX());
			ArrayList<Float> tyList = calculateTList(myList, l1.getCY(), l2.getCY());
			iSections = calculateIntersections(txList, tyList, mxList, myList);
		}
		ArrayList<Location> locsOnLine = locsOnLine(iSections, map);
		for (Location loc: locsOnLine) {
			if (loc.isObstacle()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * give all locations that are intersected by a line.
	 * @param iSections
	 * @param map
	 * @return
	 */
	public ArrayList<Location> locsOnLine(ArrayList<Pair<Float, Float>> iSections, Map map) {
		ArrayList<Location> locsOnLine = new ArrayList<>();
		for (Pair<Float, Float> iSection: iSections) {
			Collection<Location> locsToAdd = getAttachedLocations(iSection, map);
			for (Location locToAdd: locsToAdd) {
				if (!locsOnLine.contains(locToAdd)) {
					locsOnLine.add(locToAdd);					
				}
			}
		}
		return locsOnLine;
	}

	/**
	 * determine which locations are attached to an intersection
	 * @param iSection
	 * @param map
	 * @return
	 */
	private Collection<Location> getAttachedLocations(Pair<Float, Float> iSection, Map map) {
		ArrayList<Location> locs = new ArrayList<>();
		float x = iSection.getFirst();
		float x_int = (float) Math.floor(x);
		float y = iSection.getSecond();
		float y_int = (float) Math.floor(y);
		if (x == x_int && y == y_int) { // they can not be zero
			locs.add(map.getLocation((int) x_int - 1, (int) y_int));
			locs.add(map.getLocation((int) x_int, (int) y_int - 1));
			locs.add(map.getLocation((int) x_int, (int) y_int));
			locs.add(map.getLocation((int) x_int - 1, (int) y_int - 1));
			return locs;
		}
		if (x == x_int) { //y was already integer, cannot be zero
			locs.add(map.getLocation((int) x_int - 1, (int) y_int));
			locs.add(map.getLocation((int) x_int, (int) y_int));
		}
		if (y == y_int) { //y was already integer, cannot be zero
			locs.add(map.getLocation((int) x_int, (int) y_int - 1));
			locs.add(map.getLocation((int) x_int, (int) y_int));
		}
		return locs;
	}

	/**
	 * calculate coordinates of the intersections with the grid
	 * @return
	 */
	public ArrayList<Pair<Float, Float>> getInterSections() {
		ArrayList<Float> mxList = calculateMs(l1.getCX(), l2.getCX());
		ArrayList<Float> myList = calculateMs(l1.getCY(), l2.getCY());
		if (isHorizontalOrVertical()) {
			return calculateHVIntersections(mxList, myList);
		}
		else {
			ArrayList<Float> txList = calculateTList(mxList, l1.getCX(), l2.getCX());
			ArrayList<Float> tyList = calculateTList(myList, l1.getCY(), l2.getCY());
			return calculateIntersections(txList, tyList, mxList, myList);
		}
	}
	
	/**
	 * calculate intersections with the grid for the special case when the line is horizontal or vertical
	 * @param mxList
	 * @param myList
	 * @return
	 */
	private ArrayList<Pair<Float, Float>> calculateHVIntersections(ArrayList<Float> mxList, ArrayList<Float> myList) {
		ArrayList<Pair<Float, Float>> intersectionList = new ArrayList<Pair<Float, Float>>();
		if (l1.getCX() == l2.getCX()) {
			for (Float f: myList) {
				intersectionList.add(new Pair<Float, Float>(l1.getCX(), f));
			}
		}
		else {
			for (Float f: mxList) {
				intersectionList.add(new Pair<Float, Float>(f, (float) l1.getCY()));
			}
		}
		return intersectionList;
	}

	/**
	 * determine whether the line is horizontal or vertical. Has same X or Y coordinates
	 * @return
	 */
	private boolean isHorizontalOrVertical() {
		return (l1.getX() == l2.getX() || l1.getY() == l2.getY());
	}

	/**
	 * Determine grid coordinates (x or y) depending on the input coordinates
	 * @return
	 */
	private ArrayList<Float> calculateMs(float coord1, float coord2) {
		ArrayList<Float> mList = new ArrayList<>();
		float minC;
		float maxC;
		if (coord1 < coord2) {
			minC = coord1;
			maxC = coord2;
		}
		else {
			minC = coord2;
			maxC = coord1;
		}
		minC += DIFF;
		while (minC < maxC) {
			mList.add(minC);
			minC += 1;
		}
		return mList;
	}
	
	/**
	 * create the list of t's, that correspond to the grid coordinate list mlist
	 * @param mList
	 * @param coord1
	 * @param coord2
	 * @return
	 */
	private ArrayList<Float> calculateTList(ArrayList<Float> mList, float coord1, float coord2) {
		ArrayList<Float> TList = new ArrayList<Float>(); 
		float denom = coord2 - coord1;
		float tm = (mList.get(0)-coord1)/denom;  // 
		TList.add(tm);
		for (int i = 1; i < mList.size(); i++) {
			tm = (mList.get(i) - coord1)/denom;
			TList.add(tm);
		}
		return TList;
	}
	
	/**
	 * calculate the list of intersections' coordinates
	 * @param txList t's corresponding to the horizontal grid lines
	 * @param tyList t's corresponding to the vertical grid lines
	 * @param mxList grid horizontal grid coordinates
	 * @param myList vertical grid coordinates
	 * @return
	 */
	private ArrayList<Pair<Float, Float>> calculateIntersections(ArrayList<Float> txList, ArrayList<Float> tyList, ArrayList<Float> mxList, ArrayList<Float> myList) {
		ArrayList<Pair<Float, Float>> intersectionList = new ArrayList<Pair<Float, Float>>();
		int xi = 0;
		int yi = 0;
		float cDiffX = l2.getCX() - l1.getCX();
		float cDiffY = l2.getCY() - l1.getCY();
		while (xi < txList.size() && yi < tyList.size()) {
			if (txList.get(xi) < tyList.get(yi)) {
				intersectionList.add(new Pair<Float, Float>(mxList.get(xi),l1.getCY() + txList.get(xi) * cDiffY));
				xi++;
			}
			else {
				intersectionList.add(new Pair<Float, Float>(l1.getCX() + tyList.get(yi) * cDiffX, myList.get(yi)));
				yi++;
			}
		}
		// one list finished
		if (xi >= txList.size()) {
			while (yi < tyList.size()) {
				intersectionList.add(new Pair<Float, Float>(l1.getCX() + tyList.get(yi) * cDiffX, myList.get(yi)));
				yi++;
			}
		}
		if ((yi >= tyList.size())) {
			while (xi < txList.size()) {
				intersectionList.add(new Pair<Float, Float>(mxList.get(xi), l1.getCY() + txList.get(xi) * cDiffY));
				xi++;
			}
		}
		return intersectionList;
	}
	
	public String toString() {
		return l1.toString() + ", " + l2.toString();
	}
	
	
	
}
