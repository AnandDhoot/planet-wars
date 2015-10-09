import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MyBot
{
	private static int turnCounter = 0;

	public static ArrayList<Planet> knapsack(ArrayList<Planet> planets,
			int maxWeight)
	{
		ArrayList<Integer> weights = new ArrayList<Integer>();
		ArrayList<Integer> values = new ArrayList<Integer>();

		// solve 0-1 knapsack problem
		for (Planet p : planets)
		{
			// here weights and values are numShips and growthRate respectively
			// TODO change value to also take into account distance and test
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
static AllDistance distData = new AllDistance();
	static void DoTurn(PlanetWars pw) throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter("output.txt", true));
		out.println("Turn Number " + turnCounter);
		out.close();

		GameTimeline gt = new GameTimeline(pw);

		turnCounter++;
		if (turnCounter == 1)
		{// First turn knapsacking and dist Init
			
			distData.initialize(pw);
			Planet my = pw.MyPlanets().get(0);
			Planet enemy = pw.EnemyPlanets().get(0);
			int maxWeight = Math.min(
					pw.Distance(my.PlanetID(), enemy.PlanetID())
							* my.GrowthRate(), my.NumShips());

			ArrayList<Planet> planets = new ArrayList<Planet>();
			for (Planet p1 : pw.Planets())
				if (pw.Distance(p1.PlanetID(), my.PlanetID()) <= pw.Distance(
						p1.PlanetID(), enemy.PlanetID()))
					planets.add(p1);

			ArrayList<Planet> toCapture = knapsack(planets, maxWeight);
			for (Planet p : toCapture)
			{
				pw.IssueOrder(my, p, p.NumShips() + 1);
			}
		}
		else
		{
			List<DefenseTasks> currentDefenseTasks = new ArrayList<DefenseTasks>();
			HashMap<Integer, Integer> planReserve = new HashMap<Integer, Integer>();
			for (Planet p : pw.MyPlanets())
			{
				List<PlanetThreat> incomingThreats = new ArrayList<PlanetThreat>();
				for (Fleet ef : pw.EnemyFleets())
				{
					if (p.PlanetID() == ef.DestinationPlanet())
					{
						int turnsRemaining = ef.TurnsRemaining();
						int numShips = ef.NumShips();
						boolean foundThreat = false;
						for (PlanetThreat pt : incomingThreats)
						{
							if (pt.turnsRemaining == turnsRemaining)
							{
								pt.AddShips(numShips);
								foundThreat = true;
								break;
							}
						}
						if (foundThreat == false)
						{
							incomingThreats.add(new PlanetThreat(
									turnsRemaining, numShips));
						}
					}
				}
				if (!incomingThreats.isEmpty())
				{
					Collections.sort(incomingThreats);
					int turnProcessed = 0;
					int startingExcessShips = 0;
					int currentExcessShips = 0;
					for (PlanetThreat pt : incomingThreats)
					{
						int turnsRemaining = pt.turnsRemaining;
						int reinforcementShips = 0;
						for (Fleet mf : pw.MyFleets())
						{
							if (p.PlanetID() == mf.DestinationPlanet()
									&& mf.TurnsRemaining() > turnProcessed
									&& mf.TurnsRemaining() <= turnsRemaining)
							{
								reinforcementShips += mf.NumShips();
							}
						}
						if (turnProcessed == 0)
						{
							reinforcementShips += (turnsRemaining * p
									.GrowthRate());
							if (reinforcementShips >= pt.numShips)
							{
								startingExcessShips = p.NumShips();
								currentExcessShips = (reinforcementShips - pt.numShips);
							}
							else
							{
								if ((reinforcementShips + p.NumShips()) >= pt.numShips)
								{
									startingExcessShips = p.NumShips()
											- (pt.numShips - reinforcementShips);
									currentExcessShips = startingExcessShips;
								}
								else
								{
									int shortfall = pt.numShips
											- (reinforcementShips + p
													.NumShips());
									currentDefenseTasks.add(new DefenseTasks(p
											.PlanetID(), turnsRemaining,
											shortfall));
								}
							}
						}
						else
						{
							int homeDefense = ((turnsRemaining - turnProcessed) * p
									.GrowthRate())
									+ currentExcessShips
									+ reinforcementShips;
							int shortfall = pt.numShips - homeDefense;

							if (shortfall > 0)
							{
								currentDefenseTasks
										.add(new DefenseTasks(p.PlanetID(),
												turnsRemaining, shortfall));
								currentExcessShips = 0;
								startingExcessShips = currentExcessShips;

							}
							else if (shortfall <= 0)
							{
								currentExcessShips = homeDefense - pt.numShips;
								if (startingExcessShips > currentExcessShips)
								{
									startingExcessShips = currentExcessShips;
								}
							}
						}
						turnProcessed = turnsRemaining;
					}
					if (startingExcessShips > 0)
					{
						planReserve.put(p.PlanetID(), startingExcessShips);
					}
				}
				else
				{
					planReserve.put(p.PlanetID(), p.NumShips());
				}
			}

			gt.printTimeline();
			for (Planet p : pw.MyPlanets())
			{
				if (gt.Future.get(p.PlanetID()).getMinimum() != planReserve
						.get(p.PlanetID()))
					System.err.println(p.PlanetID() + " "
							+ gt.Future.get(p.PlanetID()).getMinimum() + " "
							+ planReserve.get(p.PlanetID()));
//				planReserve.put(p.PlanetID(), gt.Future.get(p.PlanetID()).getMinimum());
			}

			// try matching defense needs with available ships
			// currently works on a first come, first served basis
			// could be improved by prioritizing the defense of different
			// planets based on their value

			Collections.sort(currentDefenseTasks);
			// Currently priority to closest attack
			// TODO Change to ROI or some other evaluation fxn
			int lastPlanetID = 100;
			boolean taskUnfilled = false;
			for (DefenseTasks dt : currentDefenseTasks)
			{
				HashMap<Integer, Integer> defenseMission = new HashMap<>();
				int requiredShips = dt.numShips;
				if (lastPlanetID == dt.planetID && taskUnfilled == true)
				{
					continue;
				}
				lastPlanetID = dt.planetID;

				for (Distance d : distData.getDistancesToOtherPlanets(pw.GetPlanet(dt.planetID)))
				{
					// TODO - Give priority to better future defendable planets
					// using potentials,influence maps etc.
					int pr=d.getDestination();
					if(!planReserve.containsKey(pr))continue;
					if ((pw.Distance(pr, dt.planetID) <= dt.turnsRemaining)
							&& planReserve.get(pr) > 0 && requiredShips > 0)
					{
						if (requiredShips >= planReserve.get(pr))
						{
							requiredShips -= planReserve.get(pr);
							defenseMission.put(pr, planReserve.get(pr));
						}
						else
						{
							defenseMission.put(pr, requiredShips);
							requiredShips = 0;
						}
					}
				}
				if (requiredShips == 0)
				{
					for (int dm : defenseMission.keySet())
					{
						pw.IssueOrder(pw.GetPlanet(dm),
								pw.GetPlanet(dt.planetID),
								defenseMission.get(dm));
						for (int pr : planReserve.keySet())
						{
							if (pr == dm)
							{
								planReserve.put(pr, planReserve.get(pr)
										- defenseMission.get(dm));
							}
						}
					}
					taskUnfilled = false;
					dt.RemoveShips(dt.numShips);
				}
				else
				{
					taskUnfilled = true;
				}
			}

			// if we can't defend a planet, we should consider abandoning it
			for (DefenseTasks dt : currentDefenseTasks)
			{
				if (dt.numShips > 0 && dt.turnsRemaining == 1)
				{
					Planet p = pw.GetPlanet(dt.planetID);
					if (planReserve.containsKey(dt.planetID))
					{
						planReserve.put(dt.planetID,
								planReserve.get(dt.planetID) + p.NumShips());
					}
					else
					{
						planReserve.put(dt.planetID, p.NumShips());
					}
				}
			}

			// Rage Attack
			// TODO Replace with ROI attacks based on Timeline - Main Work
			/*
			List<Planet> pList = new ArrayList<Planet>(pw.NotMyPlanets());
			while(true){
			double bestScore=Double.MIN_VALUE;
			HashMap<Integer, Integer> bestAttack = new HashMap<Integer,Integer>();
			int bestDest=0;
			for(Planet dest : pList){
				if(gt.Future.get(dest.PlanetID()).Timeline[99].owner==1)break;
				List<Distance> sortedPL= distData.getDistancesToOtherPlanets(dest);
				HashMap<Integer, Integer> attack = new HashMap<Integer,Integer>();
				int totalSpent=0;
				int totalGain=0;
				PlanetTimeline pt= gt.Future.get(dest.PlanetID()); // Change to to true deep copy
				//Formualte a attack
				for(Distance d: sortedPL ){
					Planet source = pw.GetPlanet(d.getDestination());
					if(source.Owner()==1){// its my planet
						int availShips= planReserve.get(source.PlanetID());
						int reqShips = pt.Timeline[d.getDistance()].numShips+1;
						if(reqShips> availShips){
						Fleet f= new Fleet(1, availShips, source.PlanetID(), dest.PlanetID(),d.getDistance(), d.getDistance());
						pt.receiveFleet(f, d.getDistance());
						attack.put(source.PlanetID(), availShips);
						totalSpent+=availShips;}
						else{
							Fleet f= new Fleet(1, reqShips, source.PlanetID(), dest.PlanetID(),d.getDistance(), d.getDistance());
							pt.receiveFleet(f, d.getDistance());
							attack.put(source.PlanetID(), reqShips);
							totalGain=(100-d.getDistance())*source.GrowthRate();
							totalSpent+=reqShips;
							
						}
					}
					
				}
				//Evaluate the Attack
				if(totalGain-totalSpent>bestScore){
					bestScore=totalGain-totalSpent;
					bestAttack=attack;
					bestDest=dest.PlanetID();
				}
			}
			if(bestScore==Double.MIN_VALUE)break;
			//execute best attack
			for(int i : bestAttack.keySet()){
				pw.IssueOrder(i, bestDest,bestAttack.get(i));
				planReserve.put(i, planReserve.get(i) -bestAttack.get(i) );
				gt.Future.get(bestDest).receiveFleet(new Fleet(1, bestAttack.get(i),i,
						bestDest, pw.Distance(i,bestDest), pw.Distance(
								i, bestDest)),
				pw.Distance(i, bestDest));;
			}
			pList.remove(bestDest);
			}*/
			for (int source : planReserve.keySet())
			{
				double bestScore = Double.MIN_VALUE;
				Planet finalDest = null;
				int finShips = 0;
				for (Distance d : distData.getDistancesToOtherPlanets(pw.GetPlanet(source)))
				{
					Planet dest=pw.GetPlanet(d.getDestination());
					if(dest.Owner()==1)continue;
					if (gt.Future.get(dest.PlanetID()).Timeline[99].owner != 1)
					{
						int potentialEnemyForces = 0;

						for (Planet p2 : pw.NotMyPlanets())
						{
							int myDistance = pw.Distance(source,
									dest.PlanetID());
							int thisPlanet = p2.PlanetID();
							if (thisPlanet != dest.PlanetID())
							{
								int thisDistance = pw.Distance(dest.PlanetID(),
										thisPlanet);
								if (myDistance > thisDistance
										&& gt.Future.get(thisPlanet).Timeline[(myDistance
												- thisDistance + 1)].owner == 2)
								{
									potentialEnemyForces += gt.Future
											.get(thisPlanet).Timeline[(myDistance
											- thisDistance + 1)].numShips;
								}
							}
						}

						int requiredShips = 1
								+ gt.Future.get(dest.PlanetID()).Timeline[pw
										.Distance(source, dest.PlanetID())].numShips
								+ potentialEnemyForces;
						if (requiredShips < planReserve.get(source))
						{
							double score = dest.GrowthRate()
									* (100 - pw.Distance(source,
											dest.PlanetID())) - requiredShips;
							if (dest.Owner() == 0 && turnCounter<20)
								score = 1.5*dest.GrowthRate()
										* (100 - pw.Distance(source,
												dest.PlanetID())) - requiredShips;
							if (dest.Owner() == 2&&turnCounter>50)
								score = 2.5*dest.GrowthRate()
										* (100 - pw.Distance(source,
												dest.PlanetID())) - requiredShips;
							if (score > bestScore)
							{
								bestScore = score;
								finalDest = dest;
								finShips = requiredShips;
							}
						}

					}

				}
				if (finalDest != null)
				{	if((turnCounter<40&&bestScore>0)||(turnCounter>=40)){
					planReserve.put(source, planReserve.get(source) - finShips);
					gt.Future.get(finalDest.PlanetID()).receiveFleet(
							new Fleet(1, finShips, source,
									finalDest.PlanetID(), pw.Distance(source,
											finalDest.PlanetID()), pw.Distance(
											source, finalDest.PlanetID())),
							pw.Distance(source, finalDest.PlanetID()));
					pw.IssueOrder(source, finalDest.PlanetID(), finShips);
					gt.newFrontIDs.add(finalDest.PlanetID());
				}
				}
			}

			/*
			 * for (int source : planReserve.keySet()) { if
			 * (planReserve.get(source) < 10 * pw.GetPlanet(source)
			 * .GrowthRate()) { continue; for (int pr : planReserve.keySet()){
			 * 
			 * } Planet dest = null; int bestDistance = 999999; for (Planet p :
			 * pw.EnemyPlanets()) { int dist = pw.Distance(source,
			 * p.PlanetID()); if (dist < bestDistance) { bestDistance = dist;
			 * dest = p; } } if (dest != null) { pw.IssueOrder(source,
			 * dest.PlanetID(), planReserve.get(source)); } }
			 */
			// TODO Move ships to frontlines - Charmi
			List<Planet> frontline = new ArrayList<>();
			for(Planet currPlanet : pw.MyPlanets()){
				Planet nearestEnemyPlanet = null;
				int leastDistance=100;
				for (Planet enemyPlan : pw.EnemyPlanets())
				{
					int currDistance = pw.Distance(currPlanet.PlanetID(), enemyPlan.PlanetID());
					if (leastDistance > currDistance)
					{
						nearestEnemyPlanet = enemyPlan;
						leastDistance = currDistance;
					}
				}
				if(nearestEnemyPlanet == null){
					continue;
				}
				else {
					Boolean isFrontline=true;
					for(Planet friendlyPlanet: pw.MyPlanets()){
						if(pw.Distance(nearestEnemyPlanet.PlanetID() , friendlyPlanet.PlanetID())< leastDistance   
								&& pw.Distance(currPlanet.PlanetID() , friendlyPlanet.PlanetID() ) < leastDistance ){
							isFrontline=false;
							break;
						}
					}
					if(isFrontline){
						frontline.add(currPlanet);
					}
				}
			}
			
			for(int pr: planReserve.keySet()){
				if(frontline.contains(pw.GetPlanet(pr))){continue;}
				else {
					int leastDist=100;
					Planet closestFront = null;
					for(Planet front: frontline ){
						if(pw.Distance(pr, front.PlanetID())< leastDist){
							leastDist= pw.Distance(pr, front.PlanetID());
							closestFront=front;
						}
					}
					int numShipsPR = planReserve.get(pr);

					if(closestFront!=null && numShipsPR >0){
						pw.IssueOrder(pw.GetPlanet(pr), closestFront , numShipsPR);
					}
				}
			}
			/*for (int pr : planReserve.keySet())
			{

				int leastDistance = 100;
				Planet nearestEnemyPlanet = null;
				Planet nearestFriendlyPlanet = null;
				Planet friendlyClosestToEnemy = null;
				List<Planet> closerPlanets = new ArrayList<Planet>();

				for (Planet enemyPlan : pw.EnemyPlanets())
				{
					int currDistance = pw.Distance(pr, enemyPlan.PlanetID());
					if (leastDistance > currDistance)
					{
						nearestEnemyPlanet = enemyPlan;
						leastDistance = currDistance;
					}
				}
				if (nearestEnemyPlanet == null)
				{
					continue;
				}
				else
				{
					int friendlyDistance;
					for (Planet friendlyPlanet : pw.MyPlanets())
					{
						friendlyDistance = pw.Distance(
								friendlyPlanet.PlanetID(), pr);
						if (pw.Distance(friendlyPlanet.PlanetID(),
								nearestEnemyPlanet.PlanetID()) < leastDistance)
						{
							if (friendlyDistance < leastDistance)
							{
								closerPlanets.add(friendlyPlanet);
							}
						}
					}

					for (int frontPlanet : gt.newFrontIDs)
					{
						friendlyDistance = pw.Distance(frontPlanet, pr);
						if (pw.Distance(frontPlanet,
								nearestEnemyPlanet.PlanetID()) < leastDistance)
						{
							if (friendlyDistance < leastDistance)
							{
								Planet mp = pw.GetPlanet(frontPlanet);
								closerPlanets.add(mp);
							}
						}
					}
				}
				if (!closerPlanets.isEmpty())
				{
					int shortestFDistance = 100;
					int shortestEDistance = 100;

					for (Planet cp : closerPlanets)
					{
						int currDistance = pw.Distance(
								nearestEnemyPlanet.PlanetID(), cp.PlanetID());
						if (shortestEDistance > currDistance)
						{
							shortestEDistance = currDistance;
							friendlyClosestToEnemy = cp;
						}

						currDistance = pw.Distance(pr, cp.PlanetID());
						if (shortestFDistance > currDistance)
						{
							shortestFDistance = currDistance;
							nearestFriendlyPlanet = cp;
						}
					}
					int numShipsPR = planReserve.get(pr);
					if (nearestFriendlyPlanet != null && numShipsPR > 0)
					{
						int ShortWay = pw.Distance(pr,
								friendlyClosestToEnemy.PlanetID());
						int LongWay = pw.Distance(pr,
								nearestFriendlyPlanet.PlanetID())
								+ pw.Distance(nearestEnemyPlanet.PlanetID(),
										nearestFriendlyPlanet.PlanetID());

						if ((ShortWay * 1.50) > LongWay
								&& gt.Future.get(nearestFriendlyPlanet
										.PlanetID()).Timeline[ShortWay].owner == 1)
						{
							pw.IssueOrder(pw.GetPlanet(pr),
									nearestFriendlyPlanet, numShipsPR);
						}
						else if (gt.Future
								.get(nearestFriendlyPlanet.PlanetID()).Timeline[LongWay].owner == 1)
						{
							pw.IssueOrder(pw.GetPlanet(pr),
									friendlyClosestToEnemy, numShipsPR);
						}

					}
				}
			}*/

		}
		// TODO Advanced Move Splitter

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
