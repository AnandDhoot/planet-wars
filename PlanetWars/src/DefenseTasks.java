public class DefenseTasks implements Comparable<Object>
{
	int planetID;
	int numShips;
	int turnsRemaining;

	DefenseTasks()
	{
		planetID = -1;
		numShips = 0;
		turnsRemaining = -1;
	}

	DefenseTasks(int planetID, int turnsRemaining, int numShips)
	{
		this.planetID = planetID;
		this.numShips = numShips;
		this.turnsRemaining = turnsRemaining;
	}

	public int compareTo(Object obj)
	{
		DefenseTasks anotherTask = (DefenseTasks) obj;
		return this.turnsRemaining - anotherTask.turnsRemaining;
	}

	void RemoveShips(int amount)
	{
		numShips -= amount;
	}
	
	void print()
	{
		System.err.println(planetID + " " + numShips + " " + turnsRemaining);
	}
}