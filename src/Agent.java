
public class Agent {
	
	private boolean alive = true;

	public Agent () {
		// TODO: losowanie moich cech/charakterystyki
	}
	
	public void update () {
		if (!alive)
			return;
		
		checkIfIWillLive();
		
		checkCollisions();

		computePotentialComponentByThreat();
		computePotentialComponentByExit();
		computePotentialComponentBySocialDistances();

		decideWhereToGo();
		
		updateMotorSkills();
	}

	private void checkIfIWillLive () {
		// TODO: sprawdzenie czy prze�yj� nast�pn� iteracj�
		// if (...)
		//	alive = false;
	}

	private void checkCollisions () {
		// TODO: sprawdzenie kolizji
	}

	private void computePotentialComponentByThreat () {
		// TODO: sk�adowa potencja�u od zagro�enia po�arowego 
	}

	private void computePotentialComponentByExit () {
		// TODO: sk�adowa potencja�u od ew. wyj�cia (je�li widoczne) 
	}

	private void computePotentialComponentBySocialDistances () {
		// TODO: sk�adowa potencja�u od Social Distances
	}

	private void decideWhereToGo () {
		// TODO: je�li czekamy, �eby symulowa� zmian� pr�dko�ci, przechowaj decyzj� na potem?
	}

	private void updateMotorSkills () {
		// TODO: ograniczenie zdolno�ci poruszania si� w wyniku zatrucia?
	}

}
