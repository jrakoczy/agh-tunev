package agent;

import board.Board;
import board.Board.NoPhysicsDataException;
import board.Board.Physics;
import board.Point;

public final class Agent {
	
	enum Stance {
		STAND, CROUCH, CRAWL
	}

	/** Szeroko�� elipsy Agenta w [m]. */
	public static final double BROADNESS = 0.33;

	/** Ten drugi wymiar (grubo��?) elipsy Agenta w [m]. */
	public static final double THICKNESS = 0.2;

	/**
	 * D�ugo�� wektora orientacji Agenta w [m]. Nic nie robi, tylko do
	 * rysowania.
	 */
	public static final double ORIENTATION_VECTOR = 1.0;

	/** Kat miedzy promieniami wyznaczajacymi wycinek kola bedacy sasiedztwem */
	private static final double CIRCLE_SECTOR = 45; // 360/8
	
	/** Wartosc podstawy wykorzystywana do obliczania promienia sasiedztwa za pomoca kata*/
	private static final double BASE_RADIUS_CALC = 2;

	/** Wspolczynnik wagowy obliczonego zagro�enia */
	private static final double THREAT_COEFF = 10;

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
	private static double THREAT_COMP_BEHIND = 1.5;

	/** Wspolczynnik wagowy dla potencjalnego kierunku ruchu */
	private static double THREAT_COMP_AHEAD = 1;

	/** Standardowa, poczatkowa predkosc ruchu*/
	private static double AVG_MOVING_SPEED = 1.6 / 1000;
	
	
	/**
	 * Orientacja: k�t mi�dzy wektorem "wzroku" i osi� OX w [deg]. Kiedy wynosi
	 * 0.0 deg, to Agent "patrzy" jak o� OX (jak na geometrii analitycznej).
	 * Wtedy te� sin() i cos() dzia�aj� ~intuicyjne, tak samo jak analityczne
	 * wzory. :] -- m.
	 */
	private double phi;
	
	/** Aktualna predkosc ruchu*/
	double velocity;

	/** Pozycja Agenta na planszy w rzeczywistych [m]. */
	private Point position;

	/** Referencja do planszy. */
	private Board board;

	/** Flaga informuj�ca o statusie jednostki - zywa lub martwa */
	private boolean alive;

	/** Flaga m�wi�ca o tym, czy Agentowi uda�o si� ju� ucie�. */
	boolean exited;

	/** Aktualne stezenie karboksyhemoglobiny we krwii */
	private double hbco;

	/** Czas ruchu agenta */
	private double dt; // zamienilem na pole, zeby bylo wygodniej uzywac, ale
						// nie wiem, czy nie wywalic tego jeszcze gdzies indziej

	
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

		phi = Math.random() * 360;

		alive = true;
		exited = false;
		hbco = 0;
		dt = 0;
		velocity = AVG_MOVING_SPEED;

		// TODO: Tworzenie cech osobniczych.
	}

	/**
	 * Zwraca �redni� warto�� parametru fizycznego na wybranej powierzchni --
	 * wycinka ko�a o �rodku w �rodku danego Agenta.
	 * 
	 * Koncept: 1) jedziemy ze sta�ym {@code dalpha} po ca�ym {@code alpha}; 2)
	 * dla ka�dego z tych k�t�w jedziemy ze sta�ym {@code dr} po {@code r}. 3)
	 * Bierzemy warto�� parametru w punkcie okre�lonym przez {@code dalpha} i
	 * {@code dr}, dodajemy do sumy, a na ko�cu 4) zwracamy sum� podzielon�
	 * przez liczb� wybranych w ten spos�b punkt�w.
	 * 
	 * Taki spos�b ma 2 zalety: 1) jest ultraprosty, 2) punkty bli�ej pozycji
	 * Agenta s� g�ciej rozmieszczone na wycinku, dlatego wi�ksze znaczenie ma
	 * temperatura przy nim. ^_^ (Jeszcze kwestia dobrego dobrania
	 * {@code dalpha} i {@code dr}).
	 * 
	 * @param orientation
	 *            K�t mi�dzy wektorem orientacji Agenta a osi� symetrii wycinka
	 *            ko�a. Innymi s�owy, jak chcemy wycinek po lewej r�ce danego
	 *            Agenta, to dajemy tu 90.0 [deg], jak po prawej to -90.0 [deg].
	 *            (Dlatego, �e k�ty w geometrii analitycznej rosn� przeciwnie do
	 *            ruchu wskaz�wek zegara!).
	 * @param alpha
	 *            Rozstaw "ramion" wycinka ko�a w [deg]. Jak chcemy np. 1/8
	 *            ko�a, to dajemy 45.0 [deg], w miar� oczywiste chyba. By� mo�e
	 *            warto zmieni� nazw� tego parametru.
	 * 
	 *            Nic nie stoi na przeszkodzie, �eby wywo�a� t� funkcj� z
	 *            {@code alpha == 0.0} i zdj�� �redni� tylko z linii.
	 * 
	 *            Mo�na tak�e przyj�� {@code alpha == 360.0} i policzy� �redni�
	 *            z ca�ego otoczenia, np. do wyznaczenia warunk�w �mierci
	 *            (zamiast punktowo, tylko na pozycji Agenta). ^_^
	 * @param r
	 *            Promie� ko�a, na powierzchni wycinka kt�rego obliczamy
	 *            �redni�. (Ale konstrukt j�zykowy ;b).
	 * @param what
	 *            O kt�r� wielko�� fizyczn� nam chodzi.
	 * @return
	 */
	private double getMeanPhysics(double orientation, double alpha, double r,
			Physics what) {
		if (alpha < 0)
			throw new IllegalArgumentException("alpha < 0");
		if (r < 0)
			throw new IllegalArgumentException("r < 0");

		double dalpha = 10; // [deg]
		double dr = 0.5; // [m]

		double alphaA = phi + orientation - alpha / 2;
		double alphaB = phi + orientation + alpha / 2;
		double rA = 0;
		double rB = r;

		double sum = 0.0;
		long num = 0;

		alpha = alphaA;
		// dlatego jest porzebna konstrukcja do-while, �eby to wykona�o si�
		// przynajmniej raz (nie jestem pewien czy przy k�cie zerowym by
		// zadzia�a�o z u�yciem for-a -- b��dy numeryczne: nie mo�na por�wnywa�
		// zmiennoprzecinkowych)
		do {
			double sin = Math.sin(Math.toRadians(alpha));
			double cos = Math.cos(Math.toRadians(alpha));
			r = rA;
			do {
				try {
					sum += board.getPhysics(new Point(position.x + cos * r,
							position.y + sin * r), what);
					num++;
				} catch (NoPhysicsDataException e) {
				} catch (IndexOutOfBoundsException e) {
				}
				r += dr;
			} while (r <= rB);
			alpha += dalpha;
		} while (alpha <= alphaB);

		return sum / num;
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
	public void update(double _dt) {
		if (!alive || exited)
			return;

		this.dt = _dt;
		checkIfIWillLive();

		if (alive) { // ten sam koszt, a czytelniej, przemieni�em -- m.
			makeDecision();
			move();
		}

		// jak wyszli�my poza plansz�, to wyszli�my z tunelu? exited = true
		// spowoduje zaprzestanie wy�wietlania agenta i podbicie statystyk
		// uratowanych w ka�dym razie :]
		exited = (position.x < 0 || position.y < 0
				|| position.x > board.getDimension().x || position.y > board
				.getDimension().y);
	}

	/**
	 * Sprawdza czy Agent na swojej planszy aktualnie koliduje z *czymkolwiek*
	 * (innym Agentem, przeszkod�).
	 * 
	 * U�ywanie: najpierw ustawiamy nowe {@link #position} i {@link #phi},
	 * sprawdzamy czy {@link #hasCollision()}, je�li tak, to wracamy do starych.
	 * 
	 * Koncept prawdopodobnie do modyfikacji, na razie tak zapisa�em. -- m.
	 * 
	 * @return
	 */
	public boolean hasCollision() {
		// TODO: Sprawdzanie kolizji.
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
					|| getMeanPhysics(0, 360, BROADNESS, Physics.TEMPERATURE) > LETHAL_TEMP)
				alive = false;
		} catch (IndexOutOfBoundsException e) {
			// pro�ba o dane spoza planszy
		}
	}

	/**
	 * Funkcja oblicza aktualne stezenie karboksyhemoglobiny, uwzgledniajac
	 * zdolnosci organizmu do usuwania toksyn
	 */
	private void evaluateHbCO() {
		// TODO: Dobrac odpowiednie parametry
		if (hbco > dt * CLEANSING_VELOCITY)
			hbco -= dt * CLEANSING_VELOCITY;

		try {
			// TODO: Zastanowi� si�, czy to faktycznie jest funkcja liniowa.
			hbco += dt
					* LETHAL_HbCO_CONCN
					* (board.getPhysics(position, Physics.CO) / LETHAL_CO_CONCN);
		} catch (NoPhysicsDataException e) {
			// TODO: Mo�e po prostu nic nie r�b z hbco, je�li nie mamy danych o
			// tlenku w�gla (II)? KASIU?!...
		} catch (IndexOutOfBoundsException e) {
			// pro�ba o dane spoza planszy
		}
	}

	/**
	 * 1. Oblicza wspolczynnik atrakcyjnosci dla aktualnej pozycji
	 * 2. Sprawdza wszystkie sasiedstwa wokol co CIRCLE_SECTOR [deg]
	 * 3. Jesli aktualny wybor jest najlepszy to robi podmiane
	 * 4. Zmienia pole phi agenta zgodnie z podjeta decyzja
	 */
	private void makeDecision() {
		double new_phi = phi;
		double attractivness = 0;
		try {
			attractivness = -THREAT_COEFF
					* board.getPhysics(position, Physics.TEMPERATURE);
		} catch (NoPhysicsDataException e) {
			// brak odczytu temp. z aktualnej pozycji agenta
		}

		for (double angle = -180; angle < 180; angle += CIRCLE_SECTOR) {
			double curr_attractivness = 0;
			curr_attractivness += THREAT_COEFF * computeAttractivnessComponentByThreat(angle);
			
			if(curr_attractivness > attractivness){
				attractivness = curr_attractivness;
				new_phi = angle;
			}	
		}
		
		phi = new_phi;
	}


	/**
	 * Ruszanie na podstawie podj�tej decyzji.
	 * 
	 * Na razie kompletny random (test rysowania), chodz� jak pijani troch�. ^.-
	 * 
	 * @param dt
	 */
	private void move() {	
		position.x += velocity * dt * Math.cos(Math.toRadians(phi));
		position.y += velocity * dt * Math.sin(Math.toRadians(phi));
	}

	/**
	 * Oblicza chec wyboru danego kierunku, biorac pod uwage zarowno chec ruchu
	 * w dana strone, jak i chec ucieczki od zrodla zagrozenia.
	 * 
	 * @param angle
	 *            potencjalnie obrany kierunek
	 * @return wspolczynnik atrakcyjnosci dla zadanego kierunku, im wyzszy tym
	 *         LEPIEJ
	 */

	private double computeAttractivnessComponentByThreat(double angle) {
		double attractivness_comp = 0.0;
		double r_ahead = computeRadiusByAngle(BASE_RADIUS_CALC, angle);
		double r_behind = computeRadiusByAngle(BASE_RADIUS_CALC, angle + 180);
		
		attractivness_comp -= THREAT_COMP_AHEAD
				* getMeanPhysics(angle, CIRCLE_SECTOR, r_ahead,
						Physics.TEMPERATURE);
		attractivness_comp += THREAT_COMP_BEHIND
				* getMeanPhysics(angle + 180, CIRCLE_SECTOR, r_behind,
						Physics.TEMPERATURE);
		return attractivness_comp;
	}
	
	/** Dzieki tej funkcji mozemy latwo otrzymac odpowiednia dlugosc promienia sasiedztwa,
	 * zaleznie od tego, pod jakim katem jest ono obrocone.
	 * @param base
	 * 				podstawa potegowania, ma duzy wplyw na zroznicowanie dlugosci promienia, jako ze
	 * 				zmienia sie ona wykladniczo
	 * @param angle
	 * @return dlugosc promienia
	 */
	//TODO: Dobrac odpowiednie wspolczynniki
	private double computeRadiusByAngle(double base, double angle){
		return Math.pow(base, (180 - Math.abs(angle)) / CIRCLE_SECTOR);
	}

	

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
