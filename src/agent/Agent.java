package agent;

import board.Board;
import board.Board.Barrier;
import board.Board.Exit;
import board.Board.NoPhysicsDataException;
import board.Board.Obstacle;
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

	/**
	 * Wartosc podstawy w f. wykladniczej wykorzystywana do obliczania promienia
	 * sasiedztwa za pomoca kata
	 */
	private static final double BASE_RADIUS_CALC = 1.2;

	/**
	 * Wartosc podstawy w f. wykladniczej wykorzystywana do obliczania
	 * wspolczynnika atrakcyjnosci danego kierunku. Wspolczynniki dla kierunkow
	 * o mniejszych katach, czyli takich, ktore pozwola mniej wiecej zachowac
	 * kierunek ucieczki, maja odpowiednio wieksza wartosc.
	 */
	private static final double BASE_ATTR_CALC = 1.01;

	/**
	 * Wsp�czynnik do skalowania funkcji wyk�adniczej wykorzystywanej do
	 * obliczania promienia s�siedztwa
	 */
	private static final double POW_RADIUS_COEFF = 2;

	/**
	 * Wsp�czynnik do skalowania funkcji wyk�adniczej wykorzystywanej do
	 * obliczania wspolczynnika atrakcyjnosci.
	 */
	private static final double POW_ATTR_COEFF = 1;

	/** Wspolczynnik wagowy obliczonego zagro�enia */
	private static final double THREAT_COEFF = 10;

	/**
	 * Minimalna warto�� wsp�czynnika zagro�enia powoduj�ca zmian� kierunku.
	 * Agent zawsze kieruj� si� w stron� wyj�cia, chyba �e czynniki �rodowiskowe
	 * mu na to nie pozwalaj�. Z regu�y b�dzie to warto�� ujemna.
	 */
	private static final double MIN_THREAT_VAL = THREAT_COEFF * 50;

	/**
	 * Odleglosc od wyjscia, dla ktorej agent przestaje zwracac uwage na
	 * czynniki zewnetrzne i rzuca sie do drzwi/portalu
	 */
	private static final double EXIT_RUSH_DIST = 3;

	/** Wspolczynnik wagowy odleg�o�ci od wyj�cia */
	// private static final double EXIT_COEFF = 5;

	/** Wspolczynnik wagowy dla czynnik�w spo�ecznych */
	// private static final double SOCIAL_COEFF = 0.01;

	/** Minimalna temp. przy kt�rej agent widzi ogie� */
	private static final double MIN_FLAME_TEMP = 100;

	/** Smiertelna wartosc temp. na wysokosci 1,5m */
	private static final double LETHAL_TEMP = 80;

	/** Stezenie CO w powietrzu powodujace natychmiastowy zgon [ppm] */
	private static final double LETHAL_CO_CONCN = 30000.0;

	/** Stezenie karboksyhemoglobiny we krwi powodujace natychmiastowy zgon [%] */
	private static final double LETHAL_HbCO_CONCN = 75.0;

	/** Pr�dko�� z jak� usuwane s� karboksyhemoglobiny z organizmu */
	private static final double CLEANSING_VELOCITY = 0.08;
	
	/** Wsp�czynnik funkcji przekszta�caj�cej odleg�o�� na czas reakcji*/
	private static final double REACTION_COEFF = 0.3;

	/** Pozycja Agenta na planszy w rzeczywistych [m]. */
	Point position;

	/** Aktualnie wybrane wyj�cie ewakuacyjne */
	Exit exit;

	/** Referencja do planszy. */
	Board board;
	
	/**
	 * Orientacja: k�t mi�dzy wektorem "wzroku" i osi� OX w [deg]. Kiedy wynosi
	 * 0.0 deg, to Agent "patrzy" jak o� OX (jak na geometrii analitycznej).
	 * Wtedy te� sin() i cos() dzia�aj� ~intuicyjne, tak samo jak analityczne
	 * wzory. :] -- m.
	 */
	double phi;

	/** Flaga informuj�ca o statusie jednostki - zywa lub martwa */
	private boolean alive;

	/** Flaga m�wi�ca o tym, czy Agentowi uda�o si� ju� uciec. */
	boolean exited;
	
	/**Czas, kt�ry up�ynie, nim agent podejmie decyzje o ruchu*/
	private double pre_movement_t;

	/** Aktualne stezenie karboksyhemoglobiny we krwii */
	private double hbco;

	/** Czas ruchu agenta */
	double dt; // TODO: do boarda

	/** 'Modul' ruchu agenta */
	private Motion motion;
	
	/** Charakterystyka psychiki agenta*/
	private Psyche psyche;

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
		motion = new Motion(this);
		psyche = new Psyche(this);

		phi = Math.random() * 360 - 180;

		alive = true;
		exited = false;
		hbco = 0;
		dt = 0;
		
		pre_movement_t = REACTION_COEFF * position.evalDist(board.getFireSrc()) + psyche.reaction_t;

		// TODO: Tworzenie cech osobniczych.
	}

	/**
	 * Akcje agenta w danej iteracji.
	 * 
	 * 1. Sprawdza, czy agent zyje - jesli nie, to wychodzi z funkcji.
	 * 
	 * 2. Sprawdza, czy agent nie powinien zginac w tej turze.
	 * 
	 * 3. Wybiera wyj�cie.
	 * 
	 * 4. Aktualizuje liste checkpointow.
	 * 
	 * 5. Na podstawie danych otrzymanych w poprzednim punkcie podejmuje decyzje
	 * i wykonuje ruch
	 * 
	 * @param dt
	 *            Czas w [ms] jaki up�yn�� od ostatniego update()'u. Mo�na
	 *            wykorzysta� go do policzenia przesuni�cia w tej iteracji z
	 *            zadan� warto�ci� pr�dko�ci:
	 *            {@code dx = dt * v * cos(phi); dy = dt * v * sin(phi);}
	 * @throws NoPhysicsDataException
	 */
	public void update(double _dt) throws NoPhysicsDataException {
		if (!alive || exited)
			return;

		this.dt = _dt;
		checkIfIWillLive();

		if (alive) { // ten sam koszt, a czytelniej, przemieni�em -- m.
			chooseExit();
			motion.updateCheckpoints();
			makeDecision();
			motion.move();
		}

		// jak wyszli�my poza plansz�, to wyszli�my z tunelu? exited = true
		// spowoduje zaprzestanie wy�wietlania agenta i podbicie statystyk
		// uratowanych w ka�dym razie :]
		// TODO: zmienia� na true dopiero gdy doszli�my do wyj�cia
		exited = (position.x < 0 || position.y < 0
				|| position.x > board.getDimension().x || position.y > board
				.getDimension().y);
	}

	/**
	 * 
	 * @return aktualna pozycja
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * 
	 * @return obrot wzg OX
	 */
	public double getOrientation() {
		return phi;
	}

	/**
	 * 
	 * @return stan zdrowia
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Okresla, czy agent przezyje, sprawdzajac temperature otoczenia i stezenie
	 * toksyn we krwii
	 */
	private void checkIfIWillLive() {
		evaluateHbCO();

		if (hbco > LETHAL_HbCO_CONCN
				|| getMeanPhysics(0, 360, BROADNESS, Physics.TEMPERATURE) > LETHAL_TEMP)
			alive = false;
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
		}
	}

	/**
	 * Podejmuje decyzje, co do kierunku ruchu lub ustala nowy checkpoint.
	 */
	private void makeDecision() {
		phi = calculateNewPhi();
		double attractivness_ahead = THREAT_COEFF * computeThreatComponent(0);
		Barrier barrier = motion.isCollision(0);

		if (distToExit(exit) > EXIT_RUSH_DIST
				&& attractivness_ahead > MIN_THREAT_VAL && barrier == null) {

			double attractivness = Double.POSITIVE_INFINITY;
			for (double angle = -180; angle < 180; angle += CIRCLE_SECTOR) {
				if (angle == 0)
					continue;

				double attr_coeff = 1 / computeMagnitudeByAngle(POW_ATTR_COEFF,
						BASE_ATTR_CALC, angle);
				double curr_attractivness = THREAT_COEFF * attr_coeff
						* computeThreatComponent(angle);

				if (curr_attractivness < attractivness
						&& motion.isCollision(angle) == null) {

					attractivness = curr_attractivness;
					phi += angle;
				}
			}
		}

		if (barrier instanceof Obstacle)
			motion.addCheckpoint(motion.avoidCollision((Obstacle) barrier));
	}

	/**
	 * Metoda obliczaj�ca k�t, kt�ry agent musi obra�, by skierowa� si� do
	 * wybranego checkpoint. K�t jest wyznaczony przez o� X i odcinek ��cz�cy
	 * najblizszy checkpoint z aktualn� pozycj� agenta. Korzysta z funkcji
	 * atan2(), kt�ra w przeciwie�stwie do atan() uwzgl�dnia orientacj� na
	 * p�aszczy�nie.
	 * 
	 * @return k�t zawart w przedziale [-180, 180)
	 */
	private double calculateNewPhi() {
		if (motion.checkpoints.isEmpty()) // TODO: chyba tak ma by�, nie by�o
											// tego sprawdzenia i wywala�o
											// ArrayIndexOutOfBoundsException --
											// m.
			return phi;

		Point checkpoint = motion.checkpoints
				.get(motion.checkpoints.size() - 1);
		double deltaY = checkpoint.y - position.y;
		double deltaX = checkpoint.x - position.x;

		double angle = Math.atan2(deltaY, deltaX);
		if (angle < -Math.PI) // TODO: to chyba mozna usunac
			angle = (angle % Math.PI) + Math.PI;

		return Math.toDegrees(angle);
	}

	/**
	 * Wyb�r jednego z dw�ch najbli�szych wyj�� w zale�no�ci od odleg�o�ci i
	 * mo�liwo�ci przej�cia
	 * 
	 * @throws NoPhysicsDataException
	 */
	private void chooseExit() throws NoPhysicsDataException {
		Exit chosen_exit1 = getNearestExit(-1);
		Exit chosen_exit2 = getNearestExit(distToExit(chosen_exit1));

		//TODO: doda�em jeszcze check na null, wywala�o NullPointerException
		if ((chosen_exit1 != null && checkForBlockage(chosen_exit1) > 0)
				&& chosen_exit2 != null)
			exit = chosen_exit2;
		else
			exit = chosen_exit1;

	}

	/**
	 * Bierze pod uwage odleg�o�ci na tylko jednej osi. Szuka najbli�szego
	 * wyj�cia w odleg�o�ci nie mniejszej ni� dist. Pozwala to na szukanie wyj��
	 * b�d�cych alternatywami. Dla min_dist mniejszego od 0 szuka po prostu
	 * najbli�szego wyj�cia
	 * 
	 * @param min_dist
	 *            zadana minimalna odleg�o��
	 * @return najbli�sze wyj�cie spe�niaj�ce warunki
	 */
	// TODO: priv
	public Exit getNearestExit(double min_dist) {
		double shortest_dist = board.getDimension().x + board.getDimension().y;
		Exit nearest_exit = null;

		for (Exit e : board.getExits()) {
			double dist = Math.abs(distToExit(e));
			if (dist < shortest_dist && dist > min_dist) {
				shortest_dist = dist;
				nearest_exit = e;
			}
		}
		return nearest_exit;
	}

	/**
	 * Algorytm dzia�a, poruszaj�c sie po dw�ch osiach: Y - zawsze, X - je�li
	 * znajdzie blokad�. Zaczyna od wspolrz�dnej Y agenta i porszuamy si� po tej
	 * osi w stron� potencjalnego wyj�cia. Je�li natrafi na przeszkod�, to
	 * sprawdza, czy ca�a szeroko�� tunelu dla tej warto�ci Y jest zablokowana.
	 * Porszuaj�c si� po osi X o szeroko�� agenta, sprawdza, czy na ca�ym
	 * odcinku o d�. r�wnej szeroko�ci tunelu znajduj� si� blokady. Je�li
	 * znajdzie si� cho� jeden przesmyk - przej�cie istnieje -> sprawdzamy
	 * kolejne punkty na osi Y. Je�li nie istnieje, metoda zwraca wspolrzedna Y
	 * blokady.
	 * 
	 * TODO: W bardziej rzeczywistym modelu agent wybierze kierunek przeciwny do
	 * �r�d�a ognia.
	 * 
	 * @param _exit
	 *            wyj�cie, w kierunku kt�rego agent chce ucieka�
	 * @return -1 je�li drgoa do wyj�cia _exit nie jest zablokowana wspolrzedna
	 *         y blokady, jesli nie ma przejscia
	 * @throws NoPhysicsDataException
	 */
	// TODO: rework, uwaga na (....XXX__XX...)
	private double checkForBlockage(Exit _exit) {
		boolean viable_route = true;
		double exit_y = _exit.getExitY();
		double dist = Math.abs(position.y - exit_y);
		double ds = board.getDataCellDimension();

		if (position.y > exit_y)
			ds = -ds;

		// poruszamy si� po osi Y w kierunku wyj�cia
		double y_coord = position.y + ds;
		while (Math.abs(y_coord - position.y) < dist) {
			double x_coord = 0 + BROADNESS;
			double checkpoint_y_temp = 0;
			try {
				checkpoint_y_temp = board.getPhysics(
						new Point(x_coord, y_coord), Physics.TEMPERATURE);
			} catch (NoPhysicsDataException ex) {
				// nic sie nie dzieje
			}

			// poruszamy si� po osi X, je�li natrafili�my na blokad�
			if (checkpoint_y_temp > MIN_FLAME_TEMP) {
				viable_route = false;
				while (x_coord < board.getDimension().x) {
					double checkpoint_x_temp = MIN_FLAME_TEMP;
					try {
						checkpoint_x_temp = board.getPhysics(new Point(x_coord,
								y_coord), Physics.TEMPERATURE);
					} catch (NoPhysicsDataException ex) {
						// nic sie nie dzieje
					}

					if (checkpoint_x_temp < MIN_FLAME_TEMP)
						viable_route = true;

					x_coord += BROADNESS;
				}
			}
			// je�li nie ma przej�cia zwracamy wsp. Y blokady
			if (!viable_route)
				return y_coord;

			y_coord += ds;
		}
		return -1;
	}

	/**
	 * Oblicza odleglosc miedzy aktualna pozycja a wyjsciem
	 * 
	 * @param _exit
	 *            wybrane wyjscie
	 * @return odleglosc
	 */
	private double distToExit(Exit _exit) {
		if (_exit == null) // TODO: logiczne? -- m. :] (Wywala�o mi
							// NullPointerException, nie wiem ocb!)
			return Double.POSITIVE_INFINITY;
		return position.evalDist(_exit.getCentrePoint());
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
		// dlatego jest potrzebna konstrukcja do-while, �eby to wykona�o si�
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
					// nie ma danych tego typu w tym punkcie -- nie uwzgl�niaj
					// go do �redniej
				}
				r += dr;
			} while (r <= rB);
			alpha += dalpha;
		} while (alpha <= alphaB);

		return sum / num;
	}

	/**
	 * Oblicza wspolczynnik zagrozenia dla danego kierunku.
	 * 
	 * @param angle
	 *            potencjalnie obrany kierunek
	 * @return wspolczynnik atrakcyjnosci dla zadanego kierunku, im wyzszy tym
	 *         GORZEJ
	 */
	private double computeThreatComponent(double angle) {
		double attractivness_comp = 0.0;
		double r_ahead = computeMagnitudeByAngle(POW_RADIUS_COEFF,
				BASE_RADIUS_CALC, angle);

		attractivness_comp += getMeanPhysics(angle, CIRCLE_SECTOR, r_ahead, // TODO:
																			// -=
				Physics.TEMPERATURE);
		return attractivness_comp;
	}

	/**
	 * Dzieki tej funkcji mozemy latwo otrzymac odpowiednia dlugosc promienia
	 * sasiedztwa, zaleznie od tego, pod jakim katem jest ono obrocone.
	 * 
	 * @param base
	 *            podstawa potegowania, ma duzy wplyw na zroznicowanie dlugosci
	 *            promienia, jako ze zmienia sie ona wykladniczo
	 * @param angle
	 * @return dlugosc promienia
	 */
	// TODO: Dobrac odpowiednie wspolczynniki
	private double computeMagnitudeByAngle(double pow_coeff, double base,
			double angle) {
		return pow_coeff
				* Math.pow(base, (180 - Math.abs(angle)) / CIRCLE_SECTOR);
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
