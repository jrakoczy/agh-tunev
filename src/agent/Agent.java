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

	/** Wspolczynnik przeskalowujacy temperature na zagro�enie */
	static final double TEMP_THREAT_COEFF = 0.06;

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
	private static final double POW_RADIUS_COEFF = 8;

	/**
	 * Wsp�czynnik do skalowania funkcji wyk�adniczej wykorzystywanej do
	 * obliczania wspolczynnika atrakcyjnosci.
	 */
	private static final double POW_ATTR_COEFF = 1;

	/**
	 * Minimalna warto�� wsp�czynnika zagro�enia powoduj�ca zmian� kierunku.
	 * Agent zawsze kieruj� si� w stron� wyj�cia, chyba �e czynniki �rodowiskowe
	 * mu na to nie pozwalaj�.
	 */
	private static final double MIN_THREAT_VAL = 60;

	/**
	 * Odleglosc od wyjscia, dla ktorej agent przestaje zwracac uwage na
	 * czynniki zewnetrzne i rzuca sie do drzwi/portalu
	 */
	private static final double EXIT_RUSH_DIST = 3;

	/** Minimalna temp. przy kt�rej agent widzi ogie� */
	private static final double MIN_FLAME_TEMP = 70;

	/** Smiertelna wartosc temp. na wysokosci 1,5m */
	private static final double LETHAL_TEMP = 80;

	/** Stezenie CO w powietrzu powodujace natychmiastowy zgon [ppm] */
	private static final double LETHAL_CO_CONCN = 30000.0;

	/** Stezenie karboksyhemoglobiny we krwi powodujace natychmiastowy zgon [%] */
	private static final double LETHAL_HbCO_CONCN = 75.0;

	/** Pr�dko�� z jak� usuwane s� karboksyhemoglobiny z organizmu */
	private static final double CLEANSING_VELOCITY = 0.08;

	/** Wsp. do obliczania g�sto�ci dymu na podstawie st�enia CO */
	// TODO: bardzo naci�gane, ale to jest zbyt zmienne i nie ma danych
	private static final double CO_SMOKE_COEFF = 6.5;

	/** Wsp�czynnik funkcji przekszta�caj�cej odleg�o�� na czas reakcji */
	private static final double REACTION_COEFF = 0.3 * 1000; // wspolczynnik *
																// [s/ms]

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

	/** Czas, kt�ry up�ynie, nim agent podejmie decyzje o ruchu */
	private double pre_movement_t;

	/** Aktualne stezenie karboksyhemoglobiny we krwii */
	private double hbco;

	/** Czas ruchu agenta */
	double dt;

	/** 'Modul' ruchu agenta */
	private Motion motion;

	/** Charakterystyka psychiki agenta */
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

		pre_movement_t = (REACTION_COEFF
				* position.evalDist(board.getNearestFireSrc(position)) + psyche.reaction_t);
	}

	/**
	 * Akcje agenta w danej iteracji.
	 * 
	 * 1. Sprawdza, czy agent nie powinien zginac w tej turze.
	 * 
	 * 2. Wybiera wyj�cie.
	 * 
	 * 3. Aktualizuje liste checkpointow.
	 * 
	 * 4. Aktualizuje stopie� poddenerwowania agenta.
	 * 
	 * 5. Dostosowuje pr�dko�� do warunk�w z uwzgl�dnieniem poddenerwowania.
	 * 
	 * 6. Na podstawie danych otrzymanych w poprzednim punkcie podejmuje
	 * decyzje.
	 * 
	 * 7. Wykonuje ruch.
	 * 
	 * @param dt
	 *            Czas w [ms] jaki up�yn�� od ostatniego update()'u. Mo�na
	 *            wykorzysta� go do policzenia przesuni�cia w tej iteracji z
	 *            zadan� warto�ci� pr�dko�ci:
	 *            {@code dx = dt * v * cos(phi); dy = dt * v * sin(phi);}
	 * @throws NoPhysicsDataException
	 */
	public void update(double _dt) throws NoPhysicsDataException {
		this.dt = _dt;
		double curr_temp = getMeanPhysics(0, 360, BROADNESS,
				Physics.TEMPERATURE);
		double curr_co = getMeanPhysics(0, 360, BROADNESS, Physics.CO);
		checkIfIWillLive(curr_co, curr_temp);

		if (alive) {
			double smoke_density = curr_co * CO_SMOKE_COEFF;

			chooseExit();
			motion.updateCheckpoints();
			psyche.expAnxiety(TEMP_THREAT_COEFF * curr_temp);
			motion.adjustVelocity(smoke_density, psyche.anxiety);
			makeDecision();
			motion.move();
		}

		// jak wyszli�my poza plansz�, to wyszli�my z tunelu? exited = true
		// spowoduje zaprzestanie wy�wietlania agenta i podbicie statystyk
		// uratowanych w ka�dym razie :]
		// TODO: zmienia� na true dopiero gdy doszli�my do wyj�cia
		exited = (distToExit(exit) < THICKNESS)
				|| (position.x < 0 || position.y < 0
						|| position.x > board.getDimension().x || position.y > board
						.getDimension().y);
	}

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
	 * Zwraca warto�� niezale�nie od tego, czy jest na planszy, czy nie.
	 * 
	 * @return stan zdrowia
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Sprawdza czy agent jest na planszy
	 * 
	 * @return
	 */
	public boolean isExited() {
		return exited;
	}

	/**
	 * 
	 * @return czas ktory uplynie przed podjeciem ruchu
	 */
	public double getPreMoveTime() {
		return pre_movement_t;
	}

	/**
	 * Zwraca st�enie karboksyhemoglobiny we krwi
	 * 
	 * @return hbco
	 */
	public double getHBCO() {
		return hbco;
	}

	/**
	 * Zwraca aktualna pr�dko�� agenta
	 * 
	 * @return velocity
	 */
	public double getVelocity() {
		return motion.velocity;
	}

	/**
	 * Okresla, czy agent przezyje, sprawdzajac temperature otoczenia i stezenie
	 * toksyn we krwii
	 * 
	 * @param curr_co
	 *            st�enie co w bliskim otoczeniu agenta
	 * @param curr_temp
	 *            �rednia temp. w bliskim otoczeniu agenta
	 */
	private void checkIfIWillLive(double curr_co, double curr_temp) {
		evaluateHbCO(curr_co);

		if (hbco > LETHAL_HbCO_CONCN || curr_temp > LETHAL_TEMP)
			alive = false;
	}

	/**
	 * Funkcja oblicza aktualne stezenie karboksyhemoglobiny, uwzgledniajac
	 * zdolnosci organizmu do usuwania toksyn
	 */
	private void evaluateHbCO(double curr_co) {
		// TODO: Dobrac odpowiednie parametry
		if (hbco > dt * CLEANSING_VELOCITY)
			hbco -= dt * CLEANSING_VELOCITY;

		hbco += dt * LETHAL_HbCO_CONCN * (curr_co / LETHAL_CO_CONCN);
	}

	/**
	 * Podejmuje decyzje, co do kierunku ruchu lub ustala nowy checkpoint.
	 */
	private void makeDecision() {
		phi = calculateNewPhi();
		double attractivness_ahead = computeThreatComponent(0);
		Barrier barrier = motion.isStaticCollision(0);

		if (distToExit(exit) > EXIT_RUSH_DIST
				&& attractivness_ahead > MIN_THREAT_VAL && barrier == null) {

			double attractivness = Double.POSITIVE_INFINITY;
			for (double angle = -180; angle < 180; angle += CIRCLE_SECTOR) {
				if (angle == 0)
					continue;

				double attr_coeff = 1 / computeMagnitudeByAngle(POW_ATTR_COEFF,
						BASE_ATTR_CALC, angle);
				double curr_attractivness = attr_coeff
						* computeThreatComponent(angle);

				if (curr_attractivness < attractivness
						&& motion.isStaticCollision(angle) == null) {

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
	 * wybranego checkpointa. K�t jest wyznaczony przez o� X i odcinek ��cz�cy
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

		return Math.toDegrees(angle);
	}

	/**
	 * Wyb�r najbli�szego wyj�cia do kt�rego mo�liwe jest przej�cie
	 * 
	 * @throws NoPhysicsDataException
	 */
	private void chooseExit() throws NoPhysicsDataException {
		Exit chosen_exit = null;
		Exit curr_exit = getNearestExit(-1);

		do {
			chosen_exit = curr_exit;
			double dist_exit = distToExit(chosen_exit);
			curr_exit = getNearestExit(dist_exit);
		} while (checkForBlockage(chosen_exit) > 0 && curr_exit != null);

		exit = chosen_exit;

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
	private Exit getNearestExit(double min_dist) {
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
	 * Algorytm dzia�a, poruszaj�c sie po dw�ch osiach: X - zawsze, Y - je�li
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
		double exit_x = _exit.getExitX();
		double dist = Math.abs(position.x - exit_x);
		double ds = board.getDataCellDimension().x;

		if (position.x > exit_x)
			ds = -ds;

		// poruszamy si� po osi Y w kierunku wyj�cia
		double x_coord = position.x + ds;
		while (Math.abs(x_coord - position.x) < dist) {
			double y_coord = 0 + BROADNESS;
			double checkpoint_x_temp = 0;
			try {
				checkpoint_x_temp = board.getPhysics(
						new Point(x_coord, y_coord), Physics.TEMPERATURE);
			} catch (NoPhysicsDataException ex) {
				// nic sie nie dzieje
			}

			// poruszamy si� po osi Y, je�li natrafili�my na blokad�
			if (checkpoint_x_temp > MIN_FLAME_TEMP) {
				viable_route = false;
				while (y_coord < board.getDimension().y) {
					double checkpoint_y_temp = MIN_FLAME_TEMP;
					Point checkpoint_x = new Point(x_coord, y_coord);
					try {
						checkpoint_y_temp = board.getPhysics(checkpoint_x,
								Physics.TEMPERATURE);
					} catch (NoPhysicsDataException ex) {
						// nic sie nie dzieje
					}

					if (checkpoint_y_temp < MIN_FLAME_TEMP
							|| motion.isObstacleInPos(checkpoint_x) == null)
						viable_route = true;

					y_coord += BROADNESS;
				}
			}
			// je�li nie ma przej�cia zwracamy wsp. X blokady
			if (!viable_route)
				return x_coord;

			x_coord += ds;
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
	double distToExit(Exit _exit) {
		if (_exit == null)
			return Double.POSITIVE_INFINITY;
		Point exit_closest_p = _exit.getClosestPoint(position);
		return position.evalDist(exit_closest_p);
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

		attractivness_comp += getMeanPhysics(angle, CIRCLE_SECTOR, r_ahead,
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
	private double computeMagnitudeByAngle(double pow_coeff, double base,
			double angle) {
		return pow_coeff
				* Math.pow(base, (180 - Math.abs(angle)) / CIRCLE_SECTOR);
	}
}
