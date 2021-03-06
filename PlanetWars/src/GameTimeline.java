import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameTimeline
{
	HashMap<Integer, PlanetTimeline> Future; // planetId to its timeline
	List<Integer> newFrontIDs;

	GameTimeline()
	{
		Future = new HashMap<Integer, PlanetTimeline>();
		newFrontIDs = new ArrayList<Integer>();
	}

	GameTimeline(PlanetWars pw)
	{
		newFrontIDs = new ArrayList<Integer>();

		// set to the diameter of the map
		int horizon = 100;

		Future = new HashMap<Integer, PlanetTimeline>();
		for (Planet p : pw.Planets())
		{
			PlanetState s = new PlanetState(p.PlanetID(), p.Owner(),
					p.NumShips(), p.GrowthRate());
			PlanetTimeline tl = new PlanetTimeline(s, horizon);
			Future.put(p.PlanetID(), tl);
		}

		List<Fleet> fleetsByTurnsRemaining = new ArrayList<Fleet>();
		List<Fleet> origList = new ArrayList<Fleet>();
		Fleet min;

		for (Fleet f : pw.Fleets())
		{
			origList.add(f);
		}
		while (!origList.isEmpty())
		{
			min = origList.get(0);
			for (int i = 1; i < origList.size(); i++)
				if (min.TurnsRemaining() > origList.get(i).TurnsRemaining())
					min = origList.get(i);
			fleetsByTurnsRemaining.add(min);
			origList.remove(min);
		}

		for (Fleet f : fleetsByTurnsRemaining)
		{
			Future.get(f.DestinationPlanet()).receiveFleet(f,
					f.TurnsRemaining());
		}
	}

	void printTimeline() throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter("output.txt", true));
		for (Map.Entry<Integer, PlanetTimeline> entry : Future.entrySet())
		{
			out.println("Planet Number " + entry.getKey());
			entry.getValue().printTimeline(out);
		}
		out.close();
	}
}
