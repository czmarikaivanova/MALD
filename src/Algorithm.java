import java.util.ArrayList;

/**
 * abstract class that defines basic properties of search strategies
 * @author marika
 *
 */
public abstract class Algorithm {
	
	// Find a path from a give agent in which the algorithm instance is located.
	public abstract ArrayList<Location> findPath(Location start, Location target, Map map);
}
