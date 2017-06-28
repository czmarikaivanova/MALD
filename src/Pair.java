
public class Pair<T1, T2> {
	T1 v1;
	T2 v2;
	
	public Pair(T1 v1, T2 v2) {
		super();
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public Pair(Pair<T1, T2> p) {
		super();
		this.v1 = p.v1;
		this.v2 = p.v2;
	}
	
	public T1 getFirst() {
		return v1;
	}
	
	public void setFirst(T1 first) {
		this.v1 = first;
	}
	
	public T2 getSecond() {
		return v2;
	}
	
	public void setSecond(T2 second) {
		this.v2 = second;
	}
	
	public String toString() {
		return "<" + v1.toString() + "," + v2.toString() + ">";
	}
	
	
}
