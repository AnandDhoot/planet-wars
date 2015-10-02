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
		Future = new HashMap<Integer, PlanetTimeline>();
		// TODO - set variable to the diameter of the map
		int horizon = 30;

		for (Planet p : pw.Planets())
		{
//			System.err.println("Now running for planet");
//			System.err.println("Game " + p.PlanetID() + " " + p.Owner() + " " + p.NumShips());
			
			PlanetState s = new PlanetState(p.PlanetID(), p.Owner(), p.NumShips(), p.GrowthRate());
			PlanetTimeline tl = new PlanetTimeline(s, horizon);

//			System.err.println("Returned from constructor");
			
			Future.put(p.PlanetID(), tl);

//			System.err.println("Added to hashmap");
		}

		for (Fleet f : pw.Fleets())
		{
			PlanetTimeline pt = Future.get(f.DestinationPlanet());
			pt.receiveFleet(f, f.TurnsRemaining());
			Future.put(f.DestinationPlanet(), pt);
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
