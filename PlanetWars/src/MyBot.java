import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MyBot
{
	private static int turnCounter = 0;
	static AllDistance distData = new AllDistance();

	public static ArrayList<Planet> knapsack(PlanetWars pw,
			ArrayList<Planet> planets, int maxWeight)
	{

		ArrayList<Integer> values = new ArrayList<Integer>();
		ArrayList<Integer> weights = new ArrayList<Integer>();

		for (Planet p : planets)
		{
			weights.add(p.NumShips() + 1);
			values.add((100 - pw.Distance(pw.MyPlanets().get(0).PlanetID(),
					p.PlanetID()))
					* p.GrowthRate());
		}

		int[][] K = new int[weights.size() + 1][maxWeight];
		// Init DP Array
		for (int i = 0; i < maxWeight; i++)
		{
			K[0][i] = 0;
		}

		// Main Lopp DP

		for (int k = 1; k <= weights.size(); k++)
		{
			for (int y = 1; y <= maxWeight; y++)
			{

				if (y < weights.get(k - 1))
					K[k][y - 1] = K[k - 1][y - 1];
				else if (y > weights.get(k - 1))
					K[k][y - 1] = Math.max(K[k - 1][y - 1], K[k - 1][y - 1
							- weights.get(k - 1)]
							+ values.get(k - 1));
				else
					K[k][y - 1] = Math.max(K[k - 1][y - 1], values.get(k - 1));
			}
		}

		// extract planets in the solution
		int i = weights.size();
		int currentW = maxWeight - 1;
		ArrayList<Planet> targetPlanets = new ArrayList<Planet>();

		while ((i > 0) && (currentW >= 0))
		{
			if (((i == 0) && (K[i][currentW] > 0))
					|| (K[i][currentW] != K[i - 1][currentW]))
			{
				targetPlanets.add(planets.get(i - 1));
				currentW = currentW - weights.get(i - 1);
			}
			i--;
		}
		return targetPlanets;
	}

	static void DoTurn(PlanetWars pw) throws IOException
	{
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

			ArrayList<Planet> toCapture = knapsack(pw, planets, maxWeight);
			for (Planet p : toCapture)
			{
				pw.moveSplitter(gt, my, p, p.NumShips() + 1);
			}
		}
		else
		{
			List<DefenseTasks> currentDefenseTasks = new ArrayList<DefenseTasks>();
			HashMap<Integer, Integer> planReserve = new HashMap<Integer, Integer>();

			for (Planet p : pw.MyPlanets())
			{
				Integer minVal = gt.Future.get(p.PlanetID()).getMinimum();
				if (gt.Future.get(p.PlanetID()).numOwnerChanges() != 0)
					minVal = 0;
				else
					planReserve.put(p.PlanetID(), minVal);
			}

			for (Planet p : pw.Planets())
			{
				currentDefenseTasks.addAll(gt.Future.get(p.PlanetID())
						.addDefenseTasks());
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

				for (Distance d : distData.getDistancesToOtherPlanets(pw
						.GetPlanet(dt.planetID)))
				{
					// TODO - Give priority to better future defendable planets
					// using potentials,influence maps etc.
					int pr = d.getDestination();
					if (!planReserve.containsKey(pr))
						continue;
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
						pw.moveSplitter(gt, pw.GetPlanet(dm),
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

			// boolean reckless = false;
			// int sanityCheck = 100;
			//
			// if (pw.Production(1) > (pw.Production(2)) && pw.NumShips(1) <
			// (pw.NumShips(2))) {
			// sanityCheck *= 0.75;
			// }
			//
			// if (pw.Production(1) < (pw.Production(2)) && pw.NumShips(1) >
			// (pw.NumShips(2))) {
			// sanityCheck *= 1.25;
			// reckless = true;
			// }
			//
			boolean reckless = false;
			if (pw.Production(1) < (pw.Production(2))
					&& pw.NumShips(1) > (pw.NumShips(2)))
			{
				reckless = true;
			}
			// Multi Attack
			// if(turnCounter>200){
			// List<Planet> pList = new ArrayList<Planet>(pw.NotMyPlanets());
			// while (true)
			// {
			// double bestScore = Double.MIN_VALUE;
			// HashMap<Integer, Integer> bestAttack = new HashMap<Integer,
			// Integer>();
			// Planet bestDest = null;
			// for (Planet dest : pList)
			// {
			// if (gt.Future.get(dest.PlanetID()).Timeline[99].owner == 1)
			// break;
			// List<Distance> sortedPL = distData
			// .getDistancesToOtherPlanets(dest);
			// HashMap<Integer, Integer> attack = new HashMap<Integer,
			// Integer>();
			// int totalSpent = 0;
			// int totalGain = 0;
			// PlanetTimeline pt = gt.Future.get(dest.PlanetID()).copy(); //
			// Change
			// // to
			// // to
			// // true
			// // deep
			// // copy
			//
			// // Formualte a attack
			// for (Distance d : sortedPL)
			// {
			// Planet source = pw.GetPlanet(d.getDestination());
			//
			// if (source.Owner() == 1
			// && planReserve.containsKey(source.PlanetID()))
			// {// its my planet
			// System.err.println("hh");
			// int availShips = planReserve.get(source.PlanetID());
			// int reqShips = pt.Timeline[d.getDistance()].numShips + 1;
			// int potentialEnemyForces = 0;
			//
			// for (Planet p2 : pw.NotMyPlanets())
			// {
			// int myDistance = pw.Distance(source.PlanetID(),
			// dest.PlanetID());
			// int thisPlanet = p2.PlanetID();
			// if (thisPlanet != dest.PlanetID())
			// {
			// int thisDistance = pw.Distance(
			// dest.PlanetID(), thisPlanet);
			// if (myDistance > thisDistance
			// && gt.Future.get(thisPlanet).Timeline[(myDistance
			// - thisDistance + 1)].owner == 2)
			// {
			// potentialEnemyForces += gt.Future
			// .get(thisPlanet).Timeline[(myDistance
			// - thisDistance + 1)].numShips;
			// }
			// }
			// }
			// if(reckless)
			// potentialEnemyForces *= 0.5;
			//
			// reqShips += potentialEnemyForces;
			//
			// if (reqShips > availShips)
			// {
			// Fleet f = new Fleet(1, availShips,
			// source.PlanetID(), dest.PlanetID(),
			// d.getDistance(), d.getDistance());
			//
			// System.err.println(source.PlanetID() + " I");
			// pt.receiveFleet(f, d.getDistance());
			// attack.put(source.PlanetID(), availShips);
			// totalSpent += availShips;
			// }
			//
			// else
			// {
			// Fleet f = new Fleet(1, reqShips,
			// source.PlanetID(), dest.PlanetID(),
			// d.getDistance(), d.getDistance());
			//
			// pt.receiveFleet(f, d.getDistance());
			// attack.put(source.PlanetID(), reqShips);
			//
			// System.err.println(source.PlanetID() + " E");
			// totalGain = (100 - d.getDistance())
			// * source.GrowthRate();
			// totalSpent += reqShips;
			// System.err.println(source.PlanetID() + " E2");
			//
			// break;
			//
			// }
			// System.err.println(source.PlanetID());
			//
			// }
			//
			// }
			// // Evaluate the Attack
			// if (totalGain - totalSpent > bestScore)
			// {
			// bestScore = totalGain - totalSpent;
			// bestAttack = attack;
			// bestDest = dest;
			// }
			// }
			//
			// if (bestScore == Double.MIN_VALUE || bestDest == null)
			// {
			// pList.remove(bestDest);
			// break;
			// }
			//
			// System.err.println("Evaluating");
			//
			// // execute best attack
			// for (int i : bestAttack.keySet())
			// {
			// System.err.println(pw.GetPlanet(i).NumShips() + " "
			// + bestAttack.get(i));
			//
			// pw.IssueOrder(i, bestDest.PlanetID(), bestAttack.get(i));
			// planReserve.put(i, planReserve.get(i) - bestAttack.get(i));
			// gt.Future.get(bestDest.PlanetID()).receiveFleet(
			// new Fleet(1, bestAttack.get(i), i,
			// bestDest.PlanetID(), pw.Distance(i,
			// bestDest.PlanetID()), pw.Distance(
			// i, bestDest.PlanetID())),
			// pw.Distance(i, bestDest.PlanetID()));
			// }
			// pList.remove(bestDest);
			// }}

			for (int source : planReserve.keySet())
			{
				double bestScore = Double.MIN_VALUE;
				Planet finalDest = null;
				int finShips = 0;
				for (Distance d : distData.getDistancesToOtherPlanets(pw
						.GetPlanet(source)))
				{
					Planet dest = pw.GetPlanet(d.getDestination());
					if (dest.Owner() == 1)
						continue;
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

						if (reckless)
							potentialEnemyForces *= 0.5;

						int requiredShips = 1
								+ gt.Future.get(dest.PlanetID()).Timeline[pw
										.Distance(source, dest.PlanetID())].numShips
								+ potentialEnemyForces;
						if (requiredShips < planReserve.get(source))
						{
							double score = dest.GrowthRate()
									* (100 - pw.Distance(source,
											dest.PlanetID())) - requiredShips;
							if (dest.Owner() == 2)
								score = 2
										* dest.GrowthRate()
										* (100 - pw.Distance(source,
												dest.PlanetID()))
										- requiredShips;
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
				{
					if (true)
					{
						planReserve.put(source, planReserve.get(source)
								- finShips);
						gt.Future.get(finalDest.PlanetID()).receiveFleet(
								new Fleet(1, finShips, source,
										finalDest.PlanetID(), pw.Distance(
												source, finalDest.PlanetID()),
										pw.Distance(source,
												finalDest.PlanetID())),
								pw.Distance(source, finalDest.PlanetID()));
						pw.moveSplitter(gt, source, finalDest.PlanetID(),
								finShips);
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
			 * dest = p; } } if (dest != null) { pw.moveSplitter(gt, source,
			 * dest.PlanetID(), planReserve.get(source)); } }
			 */
			// Move ships to frontlines

			// List<Planet> frontline = new ArrayList<>();
			// for (Planet currPlanet : pw.MyPlanets())
			// {
			// Planet nearestEnemyPlanet = null;
			// int leastDistance = 100;
			// for (Planet enemyPlan : pw.EnemyPlanets())
			// {
			// int currDistance = pw.Distance(currPlanet.PlanetID(),
			// enemyPlan.PlanetID());
			// if (leastDistance > currDistance)
			// {
			// nearestEnemyPlanet = enemyPlan;
			// leastDistance = currDistance;
			// }
			// }
			// if (nearestEnemyPlanet == null)
			// {
			// continue;
			// }
			// else
			// {
			// Boolean isFrontline = true;
			// for (Planet friendlyPlanet : pw.MyPlanets())
			// {
			// if (pw.Distance(nearestEnemyPlanet.PlanetID(),
			// friendlyPlanet.PlanetID()) < leastDistance
			// && pw.Distance(currPlanet.PlanetID(),
			// friendlyPlanet.PlanetID()) < leastDistance)
			// {
			// isFrontline = false;
			// break;
			// }
			// }
			// if (isFrontline)
			// {
			// frontline.add(currPlanet);
			// }
			// }
			// }

			// for (int pr : planReserve.keySet())
			// {
			// if (frontline.contains(pw.GetPlanet(pr)))
			// {
			// continue;
			// }
			// else
			// {
			// int leastDist = 100;
			// Planet closestFront = null;
			// for (Planet front : frontline)
			// {
			// if (pw.Distance(pr, front.PlanetID()) < leastDist)
			// {
			// leastDist = pw.Distance(pr, front.PlanetID());
			// closestFront = front;
			// }
			// }
			// int numShipsPR = planReserve.get(pr);
			//
			// if (closestFront != null
			// && numShipsPR > 0
			// &&
			// gt.Future.get(closestFront.PlanetID()).Timeline[leastDist].owner
			// == 1)
			// {
			// pw.moveSplitter(gt, pw.GetPlanet(pr), closestFront,
			// numShipsPR);
			// }
			// }
			// }

			for (int pr : planReserve.keySet())
			{
				Planet friendlyClosestToEnemy = null;
				int minDistance = 100;
				Planet nearestFriendly = null;
				Planet nearestEnemy = null;

				List<Planet> friendlyCandidates = new ArrayList<Planet>();

				for (Planet enemyPlanet : pw.EnemyPlanets())
				{
					int currDistance = pw.Distance(pr, enemyPlanet.PlanetID());
					if (currDistance < minDistance)
					{
						minDistance = currDistance;
						nearestEnemy = enemyPlanet;
					}
				}
				if (nearestEnemy == null)
				{
					continue;
				}
				else
				{
					int friendlyDistance;
					// Among MyPlanets()
					for (Planet friendlyPlanet : pw.MyPlanets())
					{
						friendlyDistance = pw.Distance(pr,
								friendlyPlanet.PlanetID());
						if ((pw.Distance(friendlyPlanet.PlanetID(),
								nearestEnemy.PlanetID()) < minDistance)
								&& (friendlyDistance < minDistance))
						{
							friendlyCandidates.add(friendlyPlanet);
						}
					}

					// Among attacked planets
					for (int attackedPlanet : gt.newFrontIDs)
					{
						friendlyDistance = pw.Distance(pr, attackedPlanet);
						if ((pw.Distance(attackedPlanet,
								nearestEnemy.PlanetID()) < minDistance)
								&& (friendlyDistance < minDistance))
						{
							Planet mp = pw.GetPlanet(attackedPlanet);
							friendlyCandidates.add(mp);
						}
					}
				}
				if (!friendlyCandidates.isEmpty())
				{
					int minIndirectDistance = 100;
					int minDirectDistance = 100;

					for (Planet fc : friendlyCandidates)
					{
						int currDistance = pw.Distance(nearestEnemy.PlanetID(),
								fc.PlanetID());
						if (minDirectDistance > currDistance)
						{
							minDirectDistance = currDistance;
							friendlyClosestToEnemy = fc;
						}

						currDistance = pw.Distance(pr, fc.PlanetID());
						if (minIndirectDistance > currDistance)
						{
							minIndirectDistance = currDistance;
							nearestFriendly = fc;
						}
					}
					int numShipsPR = planReserve.get(pr);
					if (nearestFriendly != null && numShipsPR > 0)
					{
						int LongWay = pw.Distance(pr,
								nearestFriendly.PlanetID())
								+ pw.Distance(nearestFriendly.PlanetID(),
										nearestEnemy.PlanetID());
						int ShortWay = pw.Distance(pr,
								friendlyClosestToEnemy.PlanetID());

						if ((ShortWay * 1.50) > LongWay
								&& gt.Future.get(nearestFriendly.PlanetID()).Timeline[ShortWay + 1].owner == 1)
						{
							pw.moveSplitter(gt, pw.GetPlanet(pr),
									nearestFriendly, numShipsPR);
						}
						else if (gt.Future.get(nearestFriendly.PlanetID()).Timeline[LongWay + 1].owner == 1)
						{
							pw.moveSplitter(gt, pw.GetPlanet(pr),
									friendlyClosestToEnemy, numShipsPR);
						}
					}
				}
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
