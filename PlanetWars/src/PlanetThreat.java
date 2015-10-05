public class PlanetThreat implements Comparable<Object>
{
	int turnsRemaining;
	int numShips;

	public PlanetThreat()
	{
		turnsRemaining = -1;
		numShips = 0;
	}

	public PlanetThreat(int turnsRemaining, int numShips)
	{
		this.turnsRemaining = turnsRemaining;
		this.numShips = numShips;
	}

	public int compareTo(Object obj)
	{
		PlanetThreat anotherThreat = (PlanetThreat) obj;
		return this.turnsRemaining - anotherThreat.turnsRemaining;
	}

	public void AddShips(int amount)
	{
		numShips += amount;
	}
}
