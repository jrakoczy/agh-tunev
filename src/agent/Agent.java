package agent;

import java.util.HashMap;
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
			return values()[(int) (Math.random() * values().length)];
		}
		
		/**Zaklada ze stoimy posrodku rozy wiatrow
		 * 
		 * @return
		 * 			kierunek po obrocie w lewo
		 */
		public static Orientation turnLeft(Orientation currOrient){
			Orientation left_orient = null;
			int val_len = values().length;
			for(int i = 0; i < val_len; ++i){
				if(values()[i] == currOrient)
					left_orient = values()[(i+1) % (val_len)];
			}
			return left_orient;
		}
		
		/**Analogicznie do turnLeft(), tylko ze tym razem obrot w prawo*/
		public static Orientation turnRight(Orientation currOrient){
			Orientation right_orient = null;
			int val_len = values().length;
			for(int i = 0; i < val_len; ++i){
				if(values()[i] == currOrient){
					int index = i-1;
					if(index < 0)
						index += val_len;
					right_orient = values()[index];
				}
			}
			return right_orient;
		}
	}

	/** Wspolczynnik wagowy obliczonego zagro�enia */
	private static final double THREAT_COEFF = 10;

	/** Wspolczynnik wagowy odleg�o�ci od wyj�cia */
	private static final double EXIT_COEFF = 5;

	/** Wspolczynnik wagowy dla czynnik�w spo�ecznych */
	private static final double SOCIAL_COEFF = 0.01;
	
	/**Smiertelna wartosc temp. na wysokosci 1,5m*/
	private static final double LETHAL_TEMP = 80;

	/** Stezenie CO w powietrzu powodujace natychmiastowy zgon [ppm] */
	private static final double LETHAL_CO_CONCN = 30000.0;

	/** Stezenie karboksyhemoglobiny we krwi powodujace natychmiastowy zgon [%] */
	private static final double LETHAL_HbCO_CONCN = 75.0;

	/** Pr�dko�� z jak� usuwane s� karboksyhemoglobiny z organizmu */
	private static final double CLEANSING_VELOCITY = 0.08;

	/** Flaga informuj�ca o statusie jednostki - zywa lub martwa */
	private boolean alive;

	/** Referencja do planszy */
	private Board board;

	/**
	 * Kom�rka, w kt�rej aktualnie znajduje si� agent. Nie nadpisujemy jej
	 * r�cznie, tylko przez {@link #setPosition()}!
	 */
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
	 * @param _board
	 *            referencja do planszy
	 * @param _position
	 *            referencja to kom�rki b�d�cej pierwotn� pozycj� agenta
	 */
	// TODO: Tworzenie cech osobniczych
	public Agent(Board _board, Cell _position) {
		alive = true;
		this.board = _board;
		setPosition(_position);
		orientation = Orientation.getRandom();
		neighborhood = board.getNeighborhoods(this);
		hbco = 0;
	}

	/**
	 * Akcje agenta w danej iteracji. 1. Sprawdza, czy agent zyje - jesli nie,
	 * to wychodzi z funkcji. 2. Sprawdza, czy agent nie powinien zginac w tej
	 * turze. 3. Sprawdza jakie sa dostepne opcje ruchu. 4. Na podstawie danych
	 * otrzymanych w poprzednim punkcie podejmuje decyzje i wykouje ruch
	 */
	public void update() {
		if (!alive)
			return;

		if (checkIfIWillLive()) {
			move(createMoveOptions());
		}
	}

	/**
	 * Nie nadpisujmy {@link #position} r�cznie, tylko t� metod�. Potrzebuj� w
	 * kom�rce mie� referencj� do agenta, je�li na niej stoi (rysowanie).
	 * 
	 * @param newPosition
	 */
	public void setPosition(Cell newPosition) {
		if (position != null)
			position.removeAgent();
		position = newPosition;
		position.addAgent(this);
	}

	public boolean isAlive() {
		return alive;
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

		if (hbco > LETHAL_HbCO_CONCN || position.getTemperature() > LETHAL_TEMP)
			alive = false;

		return alive;
	}

	/**
	 * Funkcja oblicza aktualne stezenie karboksyhemoglobiny, uwzgledniajac
	 * zdolnosci organizmu do usuwania toksyn
	 */
	//TODO: Zastanowic sie, czy to faktycznie jest funkcja liniowa
	private void evaluateHbCO() {
		if (hbco > CLEANSING_VELOCITY)
			hbco -= CLEANSING_VELOCITY;

		hbco += LETHAL_HbCO_CONCN
				* (position.getCOConcentration() / LETHAL_CO_CONCN);
	}

	/**
	 * Sprawdza jakie s� dost�pne opcje ruchu, a nast�pnie szacuje, na ile sa
	 * atrakcyjne dla agenta Najpierw przeszukuje s�siednie kom�rki w
	 * poszukiwaniu przeszk�d i wybieram tylko te, kt�re s� puste. Nast�pnie
	 * szacuje wsp�czynnik atrakcyjno�ci dla ka�dej z mo�liwych opcji ruchu na
	 * podstawie zagro�enia, odleg�o�ci od wyj�cia, itd.
	 * 
	 * @return HashMapa kierunk�w wraz ze wsp�czynnikami atrakcyjno�ci
	 * */
	// TODO: dodac wiecej
	private HashMap<Direction, Double> createMoveOptions() {
		HashMap<Direction, Double> move_options = new HashMap<Direction, Double>();

		for (Map.Entry<Direction, Neighborhood> entry : neighborhood.entrySet()) {
			if (!entry.getValue().getFirstCell().isOccupied())
				move_options.put(entry.getKey(), 0.0);
		}

		for (Map.Entry<Direction, Double> entry : move_options.entrySet()) {
			Direction key = entry.getKey();
			Double attractivness = 0.0;
			attractivness += THREAT_COEFF * computeAttractivnessComponentByThreat(neighborhood.get(key));
		}

		return move_options;
	}

	/**
	 * 1. Analizuje wszystkie dostepne opcje ruchu pod katem atrakcyjnosci i dokonuje wyboru.
	 * 2. Obraca sie w kierunku ruchu.
	 * 3. Wykonuje ruch.
	 * 4. Aktualizuje sasiedztwo.
	 */
	private void move(HashMap<Direction, Double> move_options) {
		Direction dir = null;
		Double top_attractivness = null;

		for (Map.Entry<Direction, Double> entry : move_options.entrySet()) {
			Double curr_attractivness = entry.getValue();
			if (top_attractivness == null
					|| curr_attractivness < top_attractivness) {
				top_attractivness = curr_attractivness;
				dir = entry.getKey();
			}
		}

		rotate(dir);
		setPosition(neighborhood.get(dir).getFirstCell());
		neighborhood = board.getNeighborhoods(this);
	}

	/**Funkcja obraca agenta do kierunku jego ruchu*/
	//TODO: Poprawic
	private void rotate(Direction dir){
		switch(dir){
			case LEFT :
				orientation = Orientation.turnLeft(orientation);
				break;
			case RIGHT :
				orientation = Orientation.turnRight(orientation);
				break;
			case BOTTOMLEFT: case BOTTOMRIGHT: case BOTTOM:
				orientation = Orientation.turnRight(orientation);
				orientation = Orientation.turnRight(orientation);
				break;
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