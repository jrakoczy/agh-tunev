package agent;

import java.util.Random;

import board.Board;
import board.Board.NoPhysicsDataException;
import board.Board.Physics;
import board.Point;

public final class Agent {

	/**
	 * Orientacja: k�t mi�dzy wektorem "wzroku" i osi� OX w [deg]. Kiedy wynosi
	 * 0.0 deg, to Agent "patrzy" jak o� OX (jak na geometrii analitycznej).
	 * Wtedy te� sin() i cos() dzia�aj� ~intuicyjne, tak samo jak analityczne
	 * wzory. :] -- m.
	 */
	private double phi;

	/** Pozycja Agenta na planszy w rzeczywistych [m]. */
	private Point position;
	
	/** Szeroko�� elipsy Agenta w [m]. */
	public static final double BROADNESS = 0.33;
	
	/** Ten drugi wymiar (grubo��?) elipsy Agenta w [m]. */
	public static final double THICKNESS = 0.2;

	/** D�ugo�� wektora orientacji Agenta w [m]. Nic nie robi, tylko do rysowania. */
	public static final double ORIENTATION_VECTOR = 1.0;

	/** Referencja do planszy. */
	private Board board;

	/** Random number generator. */
	private Random rng;

	/** Wspolczynnik wagowy obliczonego zagro�enia */
	// private static final double THREAT_COEFF = 10;

	/** Wspolczynnik wagowy odleg�o�ci od wyj�cia */
	// private static final double EXIT_COEFF = 5;

	/** Wspolczynnik wagowy dla czynnik�w spo�ecznych */
	// private static final double SOCIAL_COEFF = 0.01;

	/** Smiertelna wartosc temp. na wysokosci 1,5m */
	private static final double LETHAL_TEMP = 80;

	/** Stezenie CO w powietrzu powodujace natychmiastowy zgon [ppm] */
	private static final double LETHAL_CO_CONCN = 30000.0;

	/** Stezenie karboksyhemoglobiny we krwi powodujace natychmiastowy zgon [%] */
	private static final double LETHAL_HbCO_CONCN = 75.0;

	/** Pr�dko�� z jak� usuwane s� karboksyhemoglobiny z organizmu */
	private static final double CLEANSING_VELOCITY = 0.08;

	/** Wspolczynnik wagowy dla kierunku przeciwnego do potencjalnego ruchu */
	// private static double THREAT_COMP_BEHIND = 0.5;

	/** Wspolczynnik wagowy dla potencjalnego kierunku ruchu */
	// private static double THREAT_COMP_AHEAD = 1;

	/** Flaga informuj�ca o statusie jednostki - zywa lub martwa */
	private boolean alive;

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
	public Agent(Board board, Point position) {
		this.board = board;
		this.position = position;

		rng = new Random();
		phi = rng.nextDouble() * 360;

		alive = true;
		hbco = 0;

		// TODO: Agent(): Tworzenie cech osobniczych.
	}

	/**
	 * Akcje agenta w danej iteracji.
	 * 
	 * 1. Sprawdza, czy agent zyje - jesli nie, to wychodzi z funkcji.
	 * 
	 * 2. Sprawdza, czy agent nie powinien zginac w tej turze.
	 * 
	 * 3. Sprawdza jakie sa dostepne opcje ruchu.
	 * 
	 * 4. Na podstawie danych otrzymanych w poprzednim punkcie podejmuje decyzje
	 * i wykonuje ruch
	 * 
	 * @param dt
	 *            Czas w [ms] jaki up�yn�� od ostatniego update()'u. Mo�na
	 *            wykorzysta� go do policzenia przesuni�cia w tej iteracji z
	 *            zadan� warto�ci� pr�dko�ci:
	 *            {@code dx = dt * v * cos(phi); dy = dt * v * sin(phi);}
	 */
	public void update(double dt) {
		if (!alive)
			return;

		checkIfIWillLive();

		if (!alive) // ten sam koszt, a czytelniej, przemieni�em -- m.
			return;

		// TODO: Agent::update(): Poruszanie si�.
		// move(createMoveOptions());
	}

	/**
	 * Sprawdza czy Agent na swojej planszy aktualnie koliduje z *czymkolwiek*
	 * (innym Agentem, przeszkod�).
	 * 
	 * U�ywanie: najpierw ustawiamy nowe {@link #position} i {@link #phi},
	 * sprawdzamy czy {@link #hasCollision()}, je�li tak, to wracamy do starych.
	 * 
	 * Prawdopodobnie do modyfikacji, na razie tak zapisa�em. -- m.
	 * 
	 * TODO: Agent::hasCollision(): Sprawdzanie kolizji.
	 * 
	 * @return
	 */
	public boolean hasCollision() {
		return false;
	}

	public Point getPosition() {
		return position;
	}

	/** Zwraca kierunek, w kt�rym zwr�cony jest agent */
	public double getOrientation() {
		return phi;
	}

	public boolean isAlive() {
		return alive;
	}

	/**
	 * Okresla, czy agent przezyje, sprawdzajac temperature otoczenia i stezenie
	 * toksyn we krwii
	 */
	private void checkIfIWillLive() {
		evaluateHbCO();

		try {
			if (hbco > LETHAL_HbCO_CONCN
					|| board.getPhysics(position, Physics.TEMPERATURE) > LETHAL_TEMP)
				alive = false;
		} catch (NoPhysicsDataException e) {
			// nie zmieniaj flagi �ycia, je�li nie mamy danych o temperaturze w
			// aktualnym punkcie przestrzeni i czasu (ale ofc. tylko gdy
			// st�enie CO pozwala prze�y�)
		}
	}

	/**
	 * Funkcja oblicza aktualne stezenie karboksyhemoglobiny, uwzgledniajac
	 * zdolnosci organizmu do usuwania toksyn
	 */
	private void evaluateHbCO() {
		// TODO: trzeba t� pr�dko�� teraz uzale�ni� od dt; je�li to by�o
		// 0.08/500 ms, to jakby ustawi� to w�a�nie na 0.08/500 i zawsze tutaj
		// mno�y� przez dt t� sta�� pr�dko��, b�dzie dzia�a�o tak samo.

		if (hbco > CLEANSING_VELOCITY)
			hbco -= CLEANSING_VELOCITY;

		// TODO: Zastanowi� si�, czy to faktycznie jest funkcja liniowa.
		try {
			hbco += LETHAL_HbCO_CONCN
					* (board.getPhysics(position, Physics.CO) / LETHAL_CO_CONCN);
		} catch (NoPhysicsDataException e) {
			// TODO: mo�e nic nie r�b z hbco, je�li nie mamy danych o tlenku
			// w�gla (II)? KASIU?!...
		}
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
	/*
	 * // TODO: Agent::createMoveOptions(): Zmodyfikowac mozliwosc pozostanie w
	 * // miejscu private HashMap<Direction, Double> createMoveOptions() {
	 * HashMap<Direction, Double> move_options = new HashMap<Direction,
	 * Double>();
	 * 
	 * for (Map.Entry<Direction, Neighborhood> entry : neighborhood.entrySet())
	 * { Cell first = entry.getValue().getFirstCell(); if (first != null &&
	 * !first.isOccupied()) move_options.put(entry.getKey(), 0.0); }
	 * 
	 * for (Map.Entry<Direction, Double> entry : move_options.entrySet()) {
	 * Direction key = entry.getKey(); Double attractivness = 0.0; attractivness
	 * += THREAT_COEFF computeAttractivnessComponentByThreat(key);
	 * move_options.put(key, attractivness); }
	 * 
	 * // prowizorka for (Map.Entry<Direction, Double> entry :
	 * move_options.entrySet()) { Direction key = entry.getKey(); double val =
	 * entry.getValue(); switch (orientation) { case NORTH: if (key ==
	 * Neighborhood.Direction.LEFT) val += RIGHT_EXIT_COEFF; break; case SOUTH:
	 * if (key == Neighborhood.Direction.RIGHT) val += RIGHT_EXIT_COEFF; break;
	 * case EAST: if (key == Neighborhood.Direction.BOTTOM) val +=
	 * RIGHT_EXIT_COEFF; break; case WEST: if (key ==
	 * Neighborhood.Direction.TOP) val += RIGHT_EXIT_COEFF; break; }
	 * move_options.put(key, val); }
	 * 
	 * return move_options; }
	 */

	/**
	 * 1. Analizuje wszystkie dostepne opcje ruchu pod katem atrakcyjnosci i
	 * dokonuje wyboru.
	 * 
	 * 2. Sprawdza, czy op�aca jej sie ruch, jesli nie to pomija kolejne
	 * instrukcje.
	 * 
	 * 2. Obraca sie w kierunku ruchu.
	 * 
	 * 3. Wykonuje ruch.
	 * 
	 * 4. Aktualizuje sasiedztwo.
	 */
	/*
	 * private void move(HashMap<Direction, Double> move_options) { Direction
	 * dir = null; Double top_attractivness = null;
	 * 
	 * for (Map.Entry<Direction, Double> entry : move_options.entrySet()) {
	 * Double curr_attractivness = entry.getValue(); if (top_attractivness ==
	 * null || curr_attractivness > top_attractivness) { top_attractivness =
	 * curr_attractivness; dir = entry.getKey(); } }
	 * 
	 * if (top_attractivness > -THREAT_COEFF * position.getTemperature()) {
	 * rotate(dir); setPosition(neighborhood.get(dir).getFirstCell());
	 * neighborhood = board.getNeighborhoods(this); } }
	 */

	/**
	 * Oblicza chec wyboru danego kierunku, biorac pod uwage zarowno chec ruchu
	 * w dana strone, jak i chec ucieczki od zrodla zagrozenia.
	 * 
	 * @param dir
	 *            potencjalnie obrany kierunek
	 * @return wspolczynnik atrakcyjnosci dla zadanego kierunku, im wyzszy tym
	 *         LEPIEJ
	 */
	/*
	 * private double computeAttractivnessComponentByThreat(Direction dir) {
	 * double attractivness_comp = 0.0; attractivness_comp -= THREAT_COMP_AHEAD
	 * neighborhood.get(dir).getTemperature(); attractivness_comp +=
	 * THREAT_COMP_BEHIND neighborhood.get(Direction.getOppositeDir(dir))
	 * .getTemperature();
	 * 
	 * return attractivness_comp; // TODO:
	 * Agent::computeAttractivnessComponentByThreat(): Rozwin��. }
	 */

	// private void computeAttractivnessComponentByExit() {
	// sk�adowa potencja�u od ew. wyj�cia (je�li widoczne)
	// }

	// private void computeAttractivnessComponentBySocialDistances() {
	// sk�adowa potencja�u od Social Distances
	// }

	// private void updateMotorSkills() {
	// ograniczenie zdolno�ci poruszania si� w wyniku zatrucia?
	// }

}
