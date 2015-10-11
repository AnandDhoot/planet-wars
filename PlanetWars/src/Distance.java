public class Distance implements Comparable<Distance>
{

	private int distance;
	private int destination;
	private int source;

	Distance(int source, int destination, int distance)
	{
		this.source = source;
		this.destination = destination;
		this.distance = distance;
	}

	@Override
	public int compareTo(Distance other)
	{
		return distance - other.distance;
	}

	int getDestination()
	{
		return destination;
	}

	int getSource()
	{
		return source;
	}

	int getDistance()
	{
		return distance;
	}

}