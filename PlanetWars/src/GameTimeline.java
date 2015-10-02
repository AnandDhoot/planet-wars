import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class GameTimeline
{
	HashMap<Integer, PlanetTimeline> Future; // planetId to its timeline

	GameTimeline()
	{
		Future = new HashMap<Integer, PlanetTimeline>();
	}

	GameTimeline(PlanetWars pw)
	{
		// TODO - set variable to the diameter of the map
		int horizon = 100;

		Future = new HashMap<Integer, PlanetTimeline>();
		for (Planet p : pw.Planets())
		{
			PlanetState s = new PlanetState(p.PlanetID(), p.Owner(),
					p.NumShips(), p.GrowthRate());
			PlanetTimeline tl = new PlanetTimeline(s, horizon);
			Future.put(p.PlanetID(), tl);
		}
		for (Fleet f : pw.Fleets())
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
