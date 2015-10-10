import java.util.*;

public class AllDistance {
	private HashMap<Integer, List<Distance>> plDistances = new HashMap<Integer, List<Distance>>();

	public List<Distance> getDistancesToOtherPlanets(Planet planet){
		return plDistances.get(planet.PlanetID());
	}
	public void initialize(PlanetWars pw){
		for (Planet planet : pw.Planets()){
			List<Distance> distancesToPlanet = new ArrayList<Distance>();		
			
			for (Planet other : pw.Planets())
				if (other!=planet)
					distancesToPlanet.add(new Distance(planet.PlanetID(), other.PlanetID(), pw.Distance(planet.PlanetID(), other.PlanetID())));
			
			Collections.sort(distancesToPlanet);
			plDistances.put(planet.PlanetID(), distancesToPlanet);
		}
	}

}