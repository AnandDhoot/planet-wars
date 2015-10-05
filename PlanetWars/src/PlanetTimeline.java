import java.io.PrintWriter;

public class PlanetTimeline
{
	PlanetState[] Timeline;

	PlanetTimeline()
	{
		Timeline = new PlanetState[0];
	}

	PlanetTimeline(PlanetState s, int horizon)
	{
		Timeline = new PlanetState[horizon];
		for (int i = 0; i < horizon; i++)
		{
			Timeline[i] = new PlanetState(s.planetID, s.owner, s.numShips,
					s.growthRate);
			if (s.owner != 0)
			{
				Timeline[i].numShips += s.growthRate * i;
			}
		}
	}

	void receiveFleet(Fleet f, int timeRemaining)
	{
		// TODO - Assumes timeRemaining > 0, always
		// Set owner, Number of Ships for all times after timeRemaining
		boolean inc = (f.Owner() == Timeline[timeRemaining].owner);
		if (inc)
			Timeline[timeRemaining].numShips += f.NumShips();
		else
		{
			Timeline[timeRemaining].numShips -= f.NumShips();
			if (Timeline[timeRemaining].numShips < 0)
			{
				Timeline[timeRemaining].numShips *= -1;
				Timeline[timeRemaining].owner = f.Owner();
			}
		}

		// Set owner, Number of Ships for all times after timeRemaining
		int own = Timeline[timeRemaining].owner;
		int noOfShips = Timeline[timeRemaining].numShips;
		int grRate = Timeline[timeRemaining].growthRate;
		for (int i = timeRemaining; i < Timeline.length; i++)
		{
			Timeline[i].owner = own;
			Timeline[i].numShips = noOfShips + grRate * (i - timeRemaining);
		}
	}

	void sendFleet(Fleet f, int timeRemaining)
	{
		// Assumes sent at time 0. That is, timeToSend > 0, always
		// Also assumes that the move is valid (you have ships to send)

		Timeline[timeRemaining].numShips -= f.NumShips();

		// Set Number of Ships for all times after timeRemaining
		int noOfShips = Timeline[timeRemaining].numShips;
		int grRate = Timeline[timeRemaining].growthRate;
		for (int i = timeRemaining; i < Timeline.length; i++)
			Timeline[i].numShips = noOfShips + grRate * (i - timeRemaining);
	}

	void printTimeline(PrintWriter out)
	{
		for (int i = 0; i < Timeline.length; i++)
		{
			out.print(i + " ");
			Timeline[i].printState(out);
			out.println();
		}
	}

	Integer getMinimum()
	{
		Integer val = Integer.MAX_VALUE;
		Integer own = Timeline[0].owner;
		for (int i = 0; i < Timeline.length; i++)
		{
			if (Timeline[i].owner != own)
				return 0;
		}
		for (int i = 0; i < Timeline.length; i++)
		{
			if (Timeline[i].numShips < val)
				val = Timeline[i].numShips;
		}
		return val;
	}
}