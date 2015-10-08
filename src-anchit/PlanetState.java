import java.io.PrintWriter;

public class PlanetState
{
	int planetID;
	int owner;
	int numShips;
	int growthRate;

	PlanetState()
	{
		planetID = -1;
		owner = 0;
		numShips = 0;
		growthRate = 0;
	}

	PlanetState(int id, int owner, int num, int grRate)
	{
		this.planetID = id;
		this.owner = owner;
		this.numShips = num;
		this.growthRate = grRate;
	}

	void printState(PrintWriter out)
	{
		out.print(owner + " " + numShips);
	}
}
