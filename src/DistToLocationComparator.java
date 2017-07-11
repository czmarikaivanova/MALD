import java.util.Comparator;

/**
	 * Compares two agents accordint to their distance to a specified location
	 * @author marika
	 *
	 */
	public class DistToLocationComparator implements Comparator<Agent> {
		BFS bfs;
		Map map;
		Location loc;
		
		public DistToLocationComparator(Map map, Location loc) {
			super();
			this.map = map;
			this.loc = loc;
			bfs = new BFS(Constants.CONSIDER_AGENTS_NONE);
		}

		@Override
		public int compare(Agent o1, Agent o2) {
			int d1 = bfs.minPathLength(o1.getCurrentLocation(), loc, map);
			int d2 = bfs.minPathLength(o2.getCurrentLocation(), loc, map);
			return Integer.compare(d1, d2);
		}
		
	}
	