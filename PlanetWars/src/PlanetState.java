
public class PlanetState
{
	int planetID;
	int owner;
	int numShips;
	int growthRate;
	
	PlanetState(int id, int owner, int num, int grRate)
	{
		this.planetID = id;
		this.owner = owner;
		this.numShips = num;
		this.growthRate = grRate;
	}
}
