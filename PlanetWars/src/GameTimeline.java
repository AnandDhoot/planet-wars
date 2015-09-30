import java.util.HashMap;

public class GameTimeline
{
	HashMap<Integer, PlanetTimeline> Future; // planetId to its timeline

	public GameTimeline()
	{
		Future = null;
	}

	GameTimeline(PlanetWars pw)
	{
		// TODO - set variable to the diameter of the map
		int horizon = 100;

		for (Planet p : pw.Planets())
		{
			PlanetTimeline tl = new PlanetTimeline(new PlanetState(
					p.PlanetID(), p.Owner(), p.NumShips(), p.GrowthRate()),
					horizon);

			Future.put(p.PlanetID(), tl);
		}

		for (Fleet f : pw.Fleets())
		{
			PlanetTimeline pt = Future.get(f.DestinationPlanet());
			pt.receiveFleet(f, f.TurnsRemaining());
			Future.put(f.DestinationPlanet(), pt);
		}

	}
}
