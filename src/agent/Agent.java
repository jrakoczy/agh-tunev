package agent;

import java.util.ArrayList;
import java.util.Map;

import agent.Neighborhood.Direction;
import board.Board;
import board.Cell;

public class Agent {

	/** D�ugo�� p�aszczyzny zajmowanej przez agenta - linia barkowa */
	private static final int AGENT_LENGTH = 3; // TODO: zastanowic sie, czy
												// kazdy agent ma te sam�
												// wielkosc

	/**
	 * Szeroko�� p�aszczyzny zajmowane przez agenta - o� prostopad�a do linii
	 * barkowej
	 */
	private static final int AGENT_WIDTH = 2;

	/** Wspolczynnik wagowy obliczonego zagro�enia */
	private static final int THREAT_COEFF = 100;

	/** Wspolczynnik wagowy odleg�o�ci od wyj�cia */
	private static final int EXIT_COEFF = 10;

	/** Wspolczynnik wagowy dla czynnik�w spo�ecznych */
	private static final int SOCIAL_COEFF = 1;

	/** Referencja do planszy */
	private Board board;

	/** Flaga informuj�ca o statusie jednostki - zywa lub martwa */
	private boolean alive;
	
	/** Agent's own direction, use only TOP,BOTTOM,LEFT,RIGHT */
	private Neighborhood.Direction direction;

	/** Otoczenie agenta pobierane przy ka�dym update()'cie */
	private Map<Direction, Neighborhood> neighborhood;

	/**
	 * Konstruktor agenta. Zmienia jego status na alive.
	 * 
	 * @param board
	 *            referencja do planszy
	 */
	// TODO: Tworzenie cech osobniczych
	public Agent(Board board) {
		alive = true;
		this.board = board;
		// TODO: losowanie moich cech/charakterystyki
	}

	/** Akcje agenta w danej iteracji */
	public void update() {
		if (!alive)
			return;
		
		// kuba, wybacz, �e Ci tu smaruj�, ale chcia�em pokaza� jak tego u�ywa� :}
		neighborhood = board.getNeighborhoods(this);
		if (neighborhood.get(Neighborhood.Direction.BOTTOM).getTemperature() < 70) {
			// jest dobrze
		}
		// koniec smarowania -- micha�

		checkIfIWillLive();

		checkCollisions();

		computePotentialComponentByThreat();
		computePotentialComponentByExit();
		computePotentialComponentBySocialDistances();

		decideWhereToGo();

		updateMotorSkills();
	}

	/** Statyczna metoda, mo�e by� wywo�ana przed utworzeniem agenta */
	public static int getWidth() {
		return AGENT_WIDTH;
	}

	/** Statyczna metoda, mo�e by� wywo�ana przed utworzeniem agenta */
	public static int getLength() {
		return AGENT_LENGTH;
	}

	private void checkIfIWillLive() {
		// TODO: sprawdzenie czy prze�yj� nast�pn� iteracj�
		// if (...)
		// alive = false;
	}

	private void checkCollisions() {
		// TODO: sprawdzenie kolizji
	}

	private void computePotentialComponentByThreat() {
		// TODO: sk�adowa potencja�u od zagro�enia po�arowego
	}

	private void computePotentialComponentByExit() {
		// TODO: sk�adowa potencja�u od ew. wyj�cia (je�li widoczne)
	}

	private void computePotentialComponentBySocialDistances() {
		// TODO: sk�adowa potencja�u od Social Distances
	}

	private void decideWhereToGo() {
		// TODO: je�li czekamy, �eby symulowa� zmian� pr�dko�ci, przechowaj
		// decyzj� na potem?
	}

	private void updateMotorSkills() {
		// TODO: ograniczenie zdolno�ci poruszania si� w wyniku zatrucia?
	}

}