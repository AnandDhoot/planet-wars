import java.util.*;

public class MyBot {
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

    public static  ArrayList<Planet> knapsack01(ArrayList<Planet> planets, int maxWeight) {
            ArrayList<int> weights;
            ArrayList<int> values;
           
            // solve 0-1 knapsack problem
            for (int it = 0; it != planets.size(); ++it) {
                // here weights and values are numShips and growthRate respectively
                // you can change this to something more complex if you like...
                Planet p = *it;       
                weights.push_back(p.NumShips() + 1);
                values.push_back(p.GrowthRate());
            }
           
            int K[weights.size()+1][maxWeight];
           
            for (int i = 0; i < maxWeight; i++) {
                K[0][i] = 0;
            }
           
            for (int k = 1; k <= weights.size(); k++) {
                for (int y = 1; y <= maxWeight; y++) {
               
                    if (y < weights[k-1]){
                      K[k][y-1] = K[k-1][y-1];
                    } 
                    else if (y > weights[k-1]){
                      K[k][y-1] = max( K[k-1][y-1], K[k-1][y-1-weights[k-1]] + values[k-1]);
                    }
                    else
                      K[k][y-1] = max(K[k-1][y-1], values[k-1]);
                }
            }

            // get the planets in the solution
            int i = weights.size();
            int currentW = maxWeight-1;
            ArrayList<Planet> markedPlanets;
           
            while ((i > 0) && (currentW >= 0)) {
                if (((i == 0) && (K[i][currentW] > 0)) || (K[i][currentW] != K[i-1][currentW])) {
                    markedPlanets.push_back(planets[i-1]);
                    currentW = currentW - weights[i-1];
                }
                i--;
            }
            return markedPlanets;
    }
 void DoTurn(PlanetWars pw) {
	// (1) If we currently have a fleet in flight, just do nothing.
	if (pw.MyFleets().size() >= 1) {
	    return;
	}
	// (2) Find my strongest planet.
	Planet source = null;
	double sourceScore = Double.MIN_VALUE;
	for (Planet p : pw.MyPlanets()) {
	    double score = (double)p.NumShips();
	    if (score > sourceScore) {
		sourceScore = score;
		source = p;
	    }
	}
	// (3) Find the weakest enemy or neutral planet.
	Planet dest = null;
	double destScore = Double.MIN_VALUE;
	for (Planet p : pw.NotMyPlanets()) {
	    double score = 1.0 / (1 + p.NumShips());
	    if (score > destScore) {
		destScore = score;
		dest = p;
	    }
	}
	// (4) Send half the ships from my strongest planet to the weakest
	// planet that I do not own.
	if (source != null && dest != null) {
	    int numShips = source.NumShips() / 2;
	    pw.IssueOrder(source, dest, numShips);
	}
    }

    public static void main(String[] args) {
	String line = "";
	String message = "";
	int c;
	try {
	    while ((c = System.in.read()) >= 0) {
		switch (c) {
		case '\n':
		    if (line.equals("go")) {
			PlanetWars pw = new PlanetWars(message);
			DoTurn(pw);
		        pw.FinishTurn();
			message = "";
		    } else {
			message += line + "\n";
		    }
		    line = "";
		    break;
		default:
		    line += (char)c;
		    break;
		}
	    }
	} catch (Exception e) {
	    // Owned.
	}
    }
}

