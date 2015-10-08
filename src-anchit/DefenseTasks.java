public class DefenseTasks implements Comparable<Object>
{
	int planetID;
	int numShips;
	int turnsRemaining;

	public DefenseTasks()
	{
		planetID = -1;
		numShips = 0;
		turnsRemaining = -1;
	}

	public DefenseTasks(int planetID, int turnsRemaining, int numShips)
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

	public void RemoveShips(int amount)
	{
		numShips -= amount;
	}
}