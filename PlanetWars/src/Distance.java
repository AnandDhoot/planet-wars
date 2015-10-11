public class Distance implements Comparable<Distance>{

	private int distance;
	private int destination;
	private int source;
	public Distance(int source, int destination, int distance) {
		this.source = source;
		this.destination = destination;
		this.distance = distance;
	}

	@Override
	public int compareTo(Distance other) {
		return distance-other.distance;
	}

	public int getDestination() {
		return destination;
	}
	public int getSource() {
		return source;
	}

	public int getDistance() {
		return distance;
	}

}