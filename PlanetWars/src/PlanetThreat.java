public class PlanetThreat implements Comparable<Object>
{
	int numShips;
	int turnsRemaining;

	PlanetThreat()
	{
		numShips = 0;
		turnsRemaining = -1;
	}

	PlanetThreat(int turnsRemaining, int numShips)
	{
		this.numShips = numShips;
		this.turnsRemaining = turnsRemaining;
	}

	public int compareTo(Object obj)
	{
		PlanetThreat anotherThreat = (PlanetThreat) obj;
		return this.turnsRemaining - anotherThreat.turnsRemaining;
	}

	void AddShips(int amount)
	{
		numShips += amount;
	}
}
