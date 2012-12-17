package agent;
import java.util.ArrayList;

import board.Board;
import board.Cell;


public class Agent {
	
	/**D�ugo�� p�aszczyzny zajmowanej przez agenta - linia barkowa*/
	private static final int AGENT_LENGTH = 3;			// TODO: zastanowic sie, czy kazdy agent ma te sam� wielkosc
	
	/** Szeroko�� p�aszczyzny zajmowane przez agenta - o� prostopad�a do linii barkowej*/
	private static final int AGENT_WIDTH = 2;
	
	/**Wspolczynnik wagowy obliczonego zagro�enia*/
	private static final int THREAT_COEFF = 100;
	
	/**Wspolczynnik wagowy odleg�o�ci od wyj�cia*/
	private static final int EXIT_COEFF = 10;
	
	/**Wspolczynnik wagowy dla czynnik�w spo�ecznych*/
	private static final int SOCIAL_COEFF = 1;
	
	/**Referencja do planszy*/
	private Board board;
	
	/**Flaga informuj�ca o statusie jednostki - zywa lub martwa*/
	private boolean alive;
	
	/**Tablica 2D kom�rek zajmowanych przez i s�siaduj�cych z agentem */
	private ArrayList<ArrayList<Cell>> surroundings;        //TODO: niezbyt fortunne rozwi�zanie
	
	
	
	/**
	 * Konstruktor agenta. Zmienia jego status na alive. 
	 * 
	 * @param _board			referencja do planszy
	 * @param _surroundings		referencja do kom�rek b�d�cych pierwotnym otoczeniem agenta
	 */
	
	//TODO: Tworzenie cech osobniczych
	public Agent (Board _board, ArrayList<ArrayList<Cell>> _surroundings) {
		alive = true;
		this.surroundings = _surroundings;
		this.board = _board;
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