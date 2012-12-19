package agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import agent.Neighborhood.Direction;
import board.Board;
import board.Cell;

public class Agent {

	/** Mo�liwa orientacja agenta */
	public enum Orientation {
		SOUTH, EAST, NORTH, WEST;

		/** Losuje orientacje */
		public static Orientation getRandom() {
			return values()[(int) Math.random() * values().length];
		}
	}

	/** Wspolczynnik wagowy obliczonego zagro�enia */
	private static final int THREAT_COEFF = 100;

	/** Wspolczynnik wagowy odleg�o�ci od wyj�cia */
	private static final int EXIT_COEFF = 10;

	/** Wspolczynnik wagowy dla czynnik�w spo�ecznych */
	private static final int SOCIAL_COEFF = 1;

	/** Stezenie CO w powietrzu powodujace natychmiastowy zgon [ppm] */
	private static final int LETHAL_CO_CONCN = 30000;

	/** Stezenie karboksyhemoglobiny we krwi powodujace natychmiastowy zgon [%] */
	private static final int LETHAL_HbCO_CONCN = 75;

	/** Pr�dko�� z jak� usuwane s� karboksyhemoglobiny z organizmu */
	private static final int CLEANSING_VELOCITY = 6;

	
	/** Flaga informuj�ca o statusie jednostki - zywa lub martwa */
	private boolean alive;

	/** Referencja do planszy */
	private Board board;

	/** Kom�rka, w kt�rej aktualnie znajduje si� agent */
	private Cell position;

	/** Kierunek, w kt�rym zwr�cony jest agent */
	private Orientation orientation;

	/** Otoczenie agenta pobierane przy ka�dym update()'cie */
	private Map<Direction, Neighborhood> neighborhood;

	/** Aktualne stezenie karboksyhemoglobiny we krwii */
	private double hbco;

	
	/**
	 * Konstruktor agenta. Inicjuje wszystkie pola niezb�dne do jego egzystencji
	 * na planszy. Pozycja jest z g�ry narzucona z poziomu Board. Orientacja
	 * zostaje wylosowana.
	 * 
	 * @param board
	 *            referencja do planszy
	 * @param position
	 *            referencja to kom�rki b�d�cej pierwotn� pozycj� agenta
	 */
	// TODO: Tworzenie cech osobniczych
	public Agent(Board _board, Cell _position) {
		alive = true;
		this.board = _board;
		this.position = _position;
		orientation = Orientation.getRandom();
		neighborhood = board.getNeighborhoods(this);
		hbco = 0;
	}

	/** Akcje agenta w danej iteracji.
	 *	1. Sprawdza, czy agent zyje - jesli nie, to wychodzi z funkcji.
	 *	2. Sprawdza, czy agent nie powinien zginac w tej turze.
	 *	3. Sprawdza jakie sa dostepne opcje ruchu.
	 *	4. Na podstawie danych otrzymanych w poprzednim punkcie podejmuje decyzje i wykouje ruch 
	 */
	public void update() {
		if (!alive)
			return;

		if (checkIfIWillLive()) {
			move(createMoveOptions());
			}	
	}

	/** Zwraca kierunek, w kt�rym zwr�cony jest agent */
	public Orientation getOrientation() {
		return orientation;
	}

	/** 
	 * Okresla, czy agent przezyje, sprawdzajac temperature otoczenia i stezenie
	 * toksyn we krwii
	 * 
	 * @return zwraca status agenta, zeby nie wykonywac potem niepotrzebnie
	 *         obliczen w update(), skoro i tak jest martwy ;)
	 */
	private boolean checkIfIWillLive() {
		evaluateHbCO();

		if (hbco > LETHAL_HbCO_CONCN || position.getTemperature() > 80)
			alive = false;

		return alive;
	}
	
	/**Funkcja oblicza aktualne stezenie karboksyhemoglobiny, uwzgledniajac
	 * zdolnosci organizmu do usuwania toksyn */
	private void evaluateHbCO() {
		if (hbco > CLEANSING_VELOCITY)
			hbco -= CLEANSING_VELOCITY;

		hbco += LETHAL_HbCO_CONCN
				* (position.getCOConcentration() / LETHAL_CO_CONCN);
	}

	/**
	 * Sprawdza jakie s� dost�pne opcje ruchu, a nast�pnie szacuje, na ile sa atrakcyjne dla agenta
	 * Najpierw przeszukuje s�siednie kom�rki w poszukiwaniu przeszk�d i wybieram tylko te,
	 * kt�re s� puste.
	 * Nast�pnie szacuje wsp�czynnik atrakcyjno�ci dla ka�dej z mo�liwych opcji ruchu na podstawie
	 * zagro�enia, odleg�o�ci od wyj�cia, itd.
	 * 
	 * @return
	 * 			HashMapa kierunk�w wraz ze wsp�czynnikami atrakcyjno�ci
	 * */
	//TODO: dodac wiecej 
	private HashMap<Direction, Double> createMoveOptions() {
		HashMap<Direction, Double> move_options = new HashMap<Direction, Double>();
		
		for (Map.Entry<Direction, Neighborhood> entry : neighborhood
				.entrySet()) {
			if (!entry.getValue().getFirstCell().isOccupied())
				move_options.put(entry.getKey(), 0.0);
		}

		for (Map.Entry<Direction, Double> entry : move_options.entrySet()) {
			Direction key = entry.getKey();	
			Double attractivness = 0.0;
			switch (key) {
				case TOP :		
					attractivness += THREAT_COEFF * computeAttractivnessComponentByThreat(neighborhood.get(Neighborhood.Direction.TOP));
					break;
				case TOPRIGHT :
					attractivness += THREAT_COEFF * computeAttractivnessComponentByThreat(neighborhood.get(Neighborhood.Direction.TOPRIGHT));
					break;
				case RIGHT :
					attractivness += THREAT_COEFF * computeAttractivnessComponentByThreat(neighborhood.get(Neighborhood.Direction.RIGHT));
					break;
				case BOTTOMRIGHT :
					attractivness += THREAT_COEFF * computeAttractivnessComponentByThreat(neighborhood.get(Neighborhood.Direction.BOTTOMRIGHT));
					break;
				case BOTTOM :
					attractivness += THREAT_COEFF * computeAttractivnessComponentByThreat(neighborhood.get(Neighborhood.Direction.BOTTOM));
					break;
				case BOTTOMLEFT :
					attractivness += THREAT_COEFF * computeAttractivnessComponentByThreat(neighborhood.get(Neighborhood.Direction.BOTTOMLEFT));
					break;
				case LEFT :
					attractivness += THREAT_COEFF * computeAttractivnessComponentByThreat(neighborhood.get(Neighborhood.Direction.LEFT));
					break;
				case TOPLEFT :
					attractivness += THREAT_COEFF * computeAttractivnessComponentByThreat(neighborhood.get(Neighborhood.Direction.TOPLEFT));
					break;			
			}
		}
		
		return move_options;
	}
	
	/**Przeszukuje dost�pne opcje, wybiera najbardziej atrakcyjna i wykonuje ruch*/
	private void move(HashMap<Direction, Double> move_options) {
		Direction dir = null;
		Double top_attractivness = null;
		
		for(Map.Entry<Direction, Double> entry : move_options.entrySet()){
			Double curr_attractivness = entry.getValue();
			if(top_attractivness == null || curr_attractivness < top_attractivness){
				top_attractivness = curr_attractivness;
				dir = entry.getKey();
			}
		}
		
		switch(dir){
		 //TODO: Zaimplementowac ruch. Potrzebuj� dodatkowych metod w Board i Cell
		}
		
	}

	private double computeAttractivnessComponentByThreat(Neighborhood neigh) {
		return neigh.getTemperature();
		// TODO: rozwinac
	}

	private void computeAttractivnessComponentByExit() {
		// TODO: sk�adowa potencja�u od ew. wyj�cia (je�li widoczne)
	}

	private void computeAttractivnessComponentBySocialDistances() {
		// TODO: sk�adowa potencja�u od Social Distances
	}

	

	private void updateMotorSkills() {
		// TODO: ograniczenie zdolno�ci poruszania si� w wyniku zatrucia?
	}

}