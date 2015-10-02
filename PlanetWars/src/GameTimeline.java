import java.util.HashMap;

public class GameTimeline
{
	HashMap<Integer, PlanetTimeline> Future ; // planetId to its timeline

	GameTimeline(PlanetWars pw)
	{
		// TODO - set variable to the diameter of the map
		int horizon = 100;
		Future = new HashMap<Integer, PlanetTimeline>();
		for (Planet p : pw.Planets())
		{ 
			PlanetTimeline tl = new PlanetTimeline(new PlanetState(
					p.PlanetID(), p.Owner(), p.NumShips(), p.GrowthRate()),
					horizon);

			Future.put(p.PlanetID(), tl);
		}
		for (Fleet f : pw.Fleets())
		{
			Future.get(f.DestinationPlanet()).receiveFleet(f, f.TurnsRemaining());
			
		}

	}
}
