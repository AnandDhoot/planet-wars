import java.io.*;
import java.util.*;

public class MyBot
{
	// The DoTurn function is where your code goes. The PlanetWars object
	// contains the state of the game, including information about all planets
	// and fleets that currently exist. Inside this function, you issue orders
	// using the pw.IssueOrder() function. For example, to send 10 ships from
	// planet 3 to planet 8, you would say pw.IssueOrder(3, 8, 10).
	//
	// There is already a basic strategy in place here. You can use it as a
	// starting point, or you can throw it out entirely and replace it with
	// your own. Check out the tutorials and articles on the contest website at
	// http://www.ai-contest.com/resources.

		static class TrialDefenseMission {
	
		public TrialDefenseMission (int planetID, int numShips) {
			this.planetID = planetID;
			this.numShips = numShips;
		}
		
		public int PlanetID() {
			return planetID;
		}
		
		public int NumShips() {
			return numShips;
		}
		  
		private int planetID;
		private int numShips;
		
	}
	
	
	static class DefenseTasks implements Comparable<Object> {
	
		public DefenseTasks (int planetID, int turnsRemaining, int numShips) {
			this.planetID = planetID;
			this.turnsRemaining = turnsRemaining;
			this.numShips = numShips;
		}
		
		public int PlanetID() {
			return planetID;
		}
		
		public int TurnsRemaining() {
			return turnsRemaining;
		}
		
		public int NumShips() {
			return numShips;
		}
		
		public void RemoveShips(int amount) {
			numShips -= amount;
		}
		
		public int compareTo(Object obj) {
			DefenseTasks anotherTask = (DefenseTasks) obj;
			int anotherTaskTurn = anotherTask.TurnsRemaining();  
			return this.turnsRemaining - anotherTaskTurn;
		}
		
		private int planetID;
		private int turnsRemaining;
		private int numShips;
		
	}
	
	static class PlanetThreat implements Comparable<Object> {
	
		public PlanetThreat (int turnsRemaining, int numShips) {
			this.turnsRemaining = turnsRemaining;
			this.numShips = numShips;
		}
		
		public int TurnsRemaining() {
			return turnsRemaining;
		}
		
		public int NumShips() {
			return numShips;
		}
		
		public void AddShips(int amount) {
			numShips += amount;
		}
		
		public int compareTo(Object obj) {
			PlanetThreat anotherThreat = (PlanetThreat) obj;
			int anotherThreatTurn = anotherThreat.TurnsRemaining();  
			return this.turnsRemaining - anotherThreatTurn;
		}
  
		private int turnsRemaining;
		private int numShips;
		
	}

	public static ArrayList<Planet> knapsack01(ArrayList<Planet> planets,
			int maxWeight)
	{
		ArrayList<Integer> weights = new ArrayList<Integer>();
		ArrayList<Integer> values = new ArrayList<Integer>();

		// solve 0-1 knapsack problem
		for (Planet p : planets)
		{
			// here weights and values are numShips and growthRate respectively
			// you can change this to something more complex if you like...
			weights.add(p.NumShips() + 1);
			values.add(p.GrowthRate());
		}

		int[][] K = new int[weights.size() + 1][maxWeight];

		for (int i = 0; i < maxWeight; i++)
		{
			K[0][i] = 0;
		}

		for (int k = 1; k <= weights.size(); k++)
		{
			for (int y = 1; y <= maxWeight; y++)
			{

				if (y < weights.get(k - 1))
				{
					K[k][y - 1] = K[k - 1][y - 1];
				}
				else if (y > weights.get(k - 1))
				{
					K[k][y - 1] = Math.max(K[k - 1][y - 1], K[k - 1][y - 1
							- weights.get(k - 1)]
							+ values.get(k - 1));
				}
				else
					K[k][y - 1] = Math.max(K[k - 1][y - 1], values.get(k - 1));
			}
		}

		// get the planets in the solution
		int i = weights.size();
		int currentW = maxWeight - 1;
		ArrayList<Planet> markedPlanets = new ArrayList<Planet>();

		while ((i > 0) && (currentW >= 0))
		{
			if (((i == 0) && (K[i][currentW] > 0))
					|| (K[i][currentW] != K[i - 1][currentW]))
			{
				markedPlanets.add(planets.get(i - 1));
				currentW = currentW - weights.get(i - 1);
			}
			i--;
		}
		return markedPlanets;
	}

	private static int turnCounter = 0;

	static void DoTurn(PlanetWars pw) throws IOException
	{
		turnCounter++;
		if (turnCounter == 1)
		{// First turn knapsacking problem
			// Calculate available ships
			PrintWriter out = new PrintWriter(new FileWriter("output.txt"));

			Planet my = pw.MyPlanets().get(0);
			Planet enemy = pw.EnemyPlanets().get(0);
			int maxWeight = Math.min(pw.Distance(my.PlanetID(), enemy.PlanetID())
					* my.GrowthRate(), my.NumShips());

			out.println(maxWeight);

			ArrayList<Planet> planets = new ArrayList<Planet>();
			for (Planet p1 : pw.Planets())
				if (pw.Distance(p1.PlanetID(), my.PlanetID()) < pw.Distance(
						p1.PlanetID(), enemy.PlanetID()))
					planets.add(p1);

			out.println("Hello");

			ArrayList<Planet> toCapture = knapsack01(planets, maxWeight);
			for (Planet p : toCapture)
			{
				pw.IssueOrder(my, p, p.NumShips() + 1);
				out.println(p.PlanetID() + " " + p.NumShips());
			}
			out.close();
		}
		else
		{
			// (1) If we currently have a fleet in flight, just do nothing.
		List<DefenseTasks> currentDefenseTasks = new ArrayList<DefenseTasks>();
		HashMap<Integer, Integer> planReserve = new HashMap<Integer,Integer>();
		for (Planet p : pw.MyPlanets()) {
	
			List<PlanetThreat> incomingThreats = new ArrayList<PlanetThreat>();
			for (Fleet ef : pw.EnemyFleets()) {
				if (p.PlanetID() == ef.DestinationPlanet()) {
					int turnsRemaining = ef.TurnsRemaining();
					int numShips = ef.NumShips();
					boolean foundThreat = false;
					for (PlanetThreat pt: incomingThreats) {
						if (pt.TurnsRemaining() == turnsRemaining) {
							pt.AddShips(numShips);
							foundThreat = true;
							break;
						}
					}
					if (foundThreat == false) {
						incomingThreats.add(new PlanetThreat(turnsRemaining, numShips));
					}
				}
			}
			if (!incomingThreats.isEmpty()) {
				Collections.sort(incomingThreats);
				int turnProcessed = 0;
				int startingExcessShips = 0;
				int currentExcessShips = 0;
				for (PlanetThreat pt: incomingThreats) {
					int turnsRemaining = pt.TurnsRemaining();
					int reinforcementShips = 0;
					for (Fleet mf : pw.MyFleets()) {
						if (p.PlanetID() == mf.DestinationPlanet() && mf.TurnsRemaining() > turnProcessed && mf.TurnsRemaining() <= turnsRemaining) {
							reinforcementShips += mf.NumShips();
						}
					}
					if (turnProcessed == 0) {
						reinforcementShips += (turnsRemaining * p.GrowthRate());
						if (reinforcementShips >= pt.NumShips()) {
							startingExcessShips = p.NumShips();
							currentExcessShips = (reinforcementShips - pt.NumShips());
						}
						else {
							if ((reinforcementShips + p.NumShips()) >= pt.NumShips()) {
								startingExcessShips = p.NumShips() - (pt.NumShips() - reinforcementShips);
								currentExcessShips = startingExcessShips;
							}
							else {
								int shortfall = pt.NumShips() - (reinforcementShips + p.NumShips());
								currentDefenseTasks.add(new DefenseTasks(p.PlanetID(), turnsRemaining, shortfall));
							}					
						}
					}
					else {
						int homeDefense = ((turnsRemaining - turnProcessed) * p.GrowthRate()) + currentExcessShips + reinforcementShips;
						int shortfall = pt.NumShips() - homeDefense;
						
						if (shortfall > 0) {
							currentDefenseTasks.add(new DefenseTasks(p.PlanetID(), turnsRemaining, shortfall));
							currentExcessShips = 0;
							startingExcessShips = currentExcessShips;
							
						}
						else if (shortfall <= 0) {
							currentExcessShips = homeDefense - pt.NumShips();
							if (startingExcessShips > currentExcessShips) {
								startingExcessShips = currentExcessShips;
							}
						}
					}
					turnProcessed = turnsRemaining;
				}
				if (startingExcessShips > 0) {
					planReserve.put(p.PlanetID(), startingExcessShips);
				}
			}
			else {
				planReserve.put(p.PlanetID(), p.NumShips());
			}
		}
		
		// try matching defense needs with available ships
		// currently works on a first come, first served basis
		// could be improved by prioritizing the defense of different planets based on their value
		
		Collections.sort(currentDefenseTasks);
		int lastPlanetID = 100;
		boolean taskUnfilled = false;
		for (DefenseTasks dt : currentDefenseTasks) {
			List<TrialDefenseMission> defenseMission = new ArrayList<TrialDefenseMission>();
			int requiredShips = dt.NumShips();
			if (lastPlanetID == dt.PlanetID() && taskUnfilled == true) {
				continue;
			}
			lastPlanetID = dt.PlanetID();
			
			for (int pr : planReserve.keySet()) {
				if ((pw.Distance(pr, dt.PlanetID()) <= dt.TurnsRemaining()) && planReserve.get(pr) > 0 && requiredShips > 0) {
					if (requiredShips >= planReserve.get(pr)) {
						requiredShips -= planReserve.get(pr);
						defenseMission.add(new TrialDefenseMission(pr, planReserve.get(pr)));
					}
					else {
						defenseMission.add(new TrialDefenseMission(pr, requiredShips));
						requiredShips = 0;
					}
				}
			}
			if (requiredShips == 0) {
				for (TrialDefenseMission dm : defenseMission) {
					pw.IssueOrder(pw.GetPlanet(dm.PlanetID()), pw.GetPlanet(dt.PlanetID()), dm.NumShips());
					for (int pr : planReserve.keySet()) {
						if (pr == dm.PlanetID()) {
							planReserve.put(pr,planReserve.get(pr)-dm.NumShips());
						}
					}
				}
				taskUnfilled = false;
				dt.RemoveShips(dt.NumShips());
			}
			else {
				taskUnfilled = true;
			}
		}
		
		// if we can't defend a planet, we should consider abandoning it
		
		for (DefenseTasks dt : currentDefenseTasks) {
			if (dt.NumShips() > 0 && dt.TurnsRemaining() == 1) {
				Planet p = pw.GetPlanet(dt.PlanetID());
				if(planReserve.containsKey(dt.planetID)){
					planReserve.put(dt.planetID, planReserve.get(dt.planetID)+p.NumShips());
				}
				else{
					planReserve.put(dt.planetID, p.NumShips());
				}
			}
		}
		//find strongest my planet
		
	 	boolean attackMode = false;
	 	if (pw.NumShips(1) > pw.NumShips(2)) {
	 		attackMode = true;
	 	}
	 	// (1) If we current have more tha numFleets fleets in flight, just do
	 	// nothing until at least one of the fleets arrives.
		int source = -1;
		double sourceScore = Double.MIN_VALUE;
		for (int p : planReserve.keySet()) {
		    double score = (double)planReserve.get(p);
		    if (score > sourceScore) {
			sourceScore = score;
			source = p;
		    }
		}
		// (3) Find the weakest enemy or neutral planet.
		Planet dest = null;
		double destScore = Double.MIN_VALUE;
		List<Planet> candidates = pw.NotMyPlanets();
		if(attackMode){
			candidates=pw.EnemyPlanets();
	
		}

		for (Planet p : candidates) {
		    double score = (double)(1 + p.GrowthRate()) / p.NumShips();
		    if (score > destScore) {
			destScore = score;
			dest = p;
		    }
		}
		// (4) Send half the ships from my strongest planet to the weakest
		// planet that I do not own.
		if (source != -1 && dest != null) {
		    int numShips =  planReserve.get(source);
		    pw.IssueOrder(source, dest.PlanetID(), numShips);
		}

		}
	}

	public static void main(String[] args)
	{
		String line = "";
		String message = "";
		int c;
		try
		{
			while ((c = System.in.read()) >= 0)
			{
				switch (c)
				{
				case '\n':
					if (line.equals("go"))
					{
						PlanetWars pw = new PlanetWars(message);
						DoTurn(pw);
						pw.FinishTurn();
						message = "";
					}
					else
					{
						message += line + "\n";
					}
					line = "";
					break;
				default:
					line += (char) c;
					break;
				}
			}
		}
		catch (Exception e)
		{
			// Owned.
		}
	}
}
