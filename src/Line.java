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
		ArrayList<Float> txList = calculateTList(mxList, l1.getCY(), l2.getCY());
		ArrayList<Float> tyList = calculateTList(myList, l1.getCY(), l2.getCY());
		return calculateIntersections(txList, tyList, mxList, myList);
	}
	
	/**
	 * Determine grid coordinates (x or y) depending on the input coordinates
	 * @return
	 */
	private ArrayList<Float> calculateMs(float coord1, float coord2) {
		ArrayList<Float> mList = new ArrayList<>();
		float minC;
		float maxC;
		if (l1.getCX() < l2.getCX()) {
			minC = l1.getCX();
			maxC = l2.getCX();
		}
		else {
			minC = l2.getCX();
			maxC = l1.getCX();
		}
		minC += DIFF;
		while (minC < maxC) {
			mList.add(minC);
			minC += DIFF;
		}
		return mList;
	}
	
	private ArrayList<Float> calculateTList(ArrayList<Float> mList, float coord1, float coord2) {
		ArrayList<Float> TList = new ArrayList<Float>(); 
		float denom = coord2 - coord1;
		float tm = (mList.get(0)-coord1)/denom;  // 
		TList.add(tm);
		for (int i = 1; i < mList.size(); i++) {
			tm += (mList.get(i) - coord1)/denom;
			TList.add(tm);
		}
		return TList;
	}
	
	private ArrayList<Pair<Float, Float>> calculateIntersections(ArrayList<Float> txList, ArrayList<Float> tyList, ArrayList<Float> mxList, ArrayList<Float> myList) {
		ArrayList<Pair<Float, Float>> intersectionList = new ArrayList<Pair<Float, Float>>();
		int xi = 0;
		int yi = 0;
		while (xi < txList.size() && yi < tyList.size()) {
			if (txList.get(xi) < tyList.get(yi)) {
				intersectionList.add(new Pair<Float, Float>(mxList.get(xi), txList.get(xi)));
				xi++;
			}
			else {
				intersectionList.add(new Pair<Float, Float>(myList.get(xi), tyList.get(xi)));
				yi++;
			}
		}
		// one list finished
		if (xi >= txList.size()) {
			while (yi < tyList.size()) {
				intersectionList.add(new Pair<Float, Float>(myList.get(xi), tyList.get(xi)));
				yi++;
			}
		}
		if ((yi >= tyList.size())) {
			intersectionList.add(new Pair<Float, Float>(mxList.get(xi), txList.get(xi)));
			yi++;
		}
		return intersectionList;
	}
	
	
}
