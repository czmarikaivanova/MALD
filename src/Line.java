import java.util.ArrayList;

public class Line {

	private Location l1;
	private Location l2;
	
	private static final double DIFF = 0.5;
	
	public Line(Location l1, Location l2) {
		this.l1 = l1;
		this.l2 = l2;

	}
	
	public ArrayList<Pair<Float, Float>> getInterSections() {
		ArrayList<Float> mxList = calculateMs(l1.getCX(), l2.getCX());
		ArrayList<Float> myList = calculateMs(l1.getCY(), l2.getCY());
		ArrayList<Float> txList = calculateTList(mxList, l1.getCX(), l2.getCX());
		ArrayList<Float> tyList = calculateTList(myList, l1.getCY(), l2.getCY());
		return calculateIntersections(txList, tyList, mxList, myList, l1, l2);
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
	
	private ArrayList<Pair<Float, Float>> calculateIntersections(ArrayList<Float> txList, ArrayList<Float> tyList, ArrayList<Float> mxList, ArrayList<Float> myList, Location l1, Location l2) {
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
