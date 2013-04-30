package edu.agh.tunev.model.cellular;

import java.util.Vector;

import edu.agh.tunev.interpolation.Interpolator;
import edu.agh.tunev.model.AbstractModel;
import edu.agh.tunev.model.cellular.agent.Person;
import edu.agh.tunev.model.cellular.grid.Board;
import edu.agh.tunev.model.cellular.grid.Cell;
import edu.agh.tunev.statistics.KilledStatistics;
import edu.agh.tunev.statistics.Statistics.AddCallback;
import edu.agh.tunev.world.Physics;
import edu.agh.tunev.world.World;
import edu.agh.tunev.world.World.ProgressCallback;

public final class Model extends AbstractModel<Person> {

	public final static String MODEL_NAME = "w�sowy automat kom�rkowy";

	public Model(World world, Interpolator interpolator) {
		super(world, interpolator);
	}

	// przyk�adowa dyskretyzacja �wiata -- czyli rozmiar jednej kom�rki na
	// planszy -- oczywi�cie w metrach -- do zmiany
	private static final double DX = 0.5;
	private static final double DY = 0.4;

	// przyk�adowa dyskretyzacja czasu -- czyli co ile czasu nasze osobniki
	// podejmuj� decyzj� o skoku? inaczej: co ile rzeczywistego czasu
	// update'ujemy stan naszego automatu -- oczywi�cie w sekundach -- do zmiany
	private static final double DT = 0.5;

	private Board board;

	@Override
	public void simulate(double duration, Vector<Person> people,
			ProgressCallback progressCallback, AddCallback addCallback) {
		// jakie s� rzeczywiste wymiary �wiata?
		double dimX = world.getXDimension();
		double dimY = world.getYDimension();

		// jakie s� dyskretne wymiary �wiata? ile kom�rek w OX i OY?
		// u�ywa funkcji do t�umaczenia wymiar�w z ci�g�ych na dyskretne z
		// uwzgl�dnieniem DX i DY. Zobacz poni�ej ich definicje.
		int numX = c2dX(dimX) + 1;
		int numY = c2dY(dimY) + 1;

		// stw�rz automat (plansz� kom�rek) o obliczonych dyskretnych wymiarach;
		board = new Board(numX, numY);

		// pozaznaczaj osoby na naszej modelowej, wewn�trznej, planszy
		for (Person p : people) {
			// w kt�rej kom�rce jest ta osoba?
			int ix = c2dX(p.getX());
			int iy = c2dY(p.getY());

			// przesu� j� dok�adnie na �rodek tej kom�rki...
			p.setPosition(d2cX(ix), d2cY(iy));
			// ... i zr�b jej tam "zdj�cie" dla interpolatora w chwili t=0[s]
			interpolator.saveState(p, 0.0);

			// zaznacz w odpowiedniej kom�rce automatu, �e kt�r� osob�
			Cell c = board.get(ix, iy);
			c.setPerson(p);
		}

		// TODO: pododawaj jakie� wykresy do UI zwi�zane z tym modelem
		//
		// sidenote: zobacz helpa do interfejsu Statistics: gdy dany wykres
		// pasuje do wielu modeli (np. liczba zabitych jako f(t)), to dodaj jego
		// klas� do pakietu tunev.statistics; je�li pasuje tylko do tego modelu,
		// to dodaj do pakietu tego modelu
		KilledStatistics killedStatistics = new KilledStatistics();
		addCallback.add(killedStatistics);

		// TODO: pozaznaczaj przeszkody na planszy

		// TODO: pozaznaczaj wyj�cia na planszy

		// kolejne iteracje automatu -- uwaga, �adnego czekania w stylu
		// Thread.sleep() -- to ma si� policzy� *jak najszybciej*! --
		// wy�wietlanie "filmu" z symulacji jest niezale�ne od obliczania (no,
		// tyle tylko zale�ne, �e mo�emy wy�wietla� tylko do momentu, kt�ry ju�
		// si� policzy�)
		int num = (int) Math.round(Math.ceil(world.getDuration() / DT));
		double t = 0;
		for (int i = 1; i <= num; i++) {
			// uaktualnij rzeczywisty czas naszej symulacji
			t += DT;

			// po�ci�gaj aktualn� fizyk� do kom�rek
			for (int ix = 0; ix < numX; ix++)
				for (int iy = 0; iy < numY; iy++) {
					Physics physics = world.getPhysicsAt(t, d2cX(ix), d2cY(iy));
					board.get(ix, iy).setPhysics(physics);
				}

			// przejd� do nast�pnego stanu automatu
			board.update();

			// por�b zdj�cia osobom w aktualnym rzeczywistym czasie
			for (Person p : people)
				interpolator.saveState(p, t);

			// TODO: uaktualnij wykresy, kt�re mog� by� aktualizowane w trakcie
			// iteracji
			int currentNumDead = 123; // prawdopodobnie ta dana ustawiana
										// gdzie indziej ;p~
			killedStatistics.add(t, currentNumDead);

			// grzeczno��: zwi�ksz ProgressBar w UI
			progressCallback.update(i, num, (i < num ? "Wci�� licz�..."
					: "Gotowe!"));
		}

		// TODO: ew. wype�nij wykresy, kt�re mog� by� wype�nione dopiero po
		// zako�czeniu symulacji

		// i tyle ^_^
	}

	/**
	 * Discreet to continuous dimensions for OX.
	 */
	private static double d2cX(int ix) {
		// zwr�� pozycj� w �rodku kom�rki
		return (0.5 + ix) * DX;
	}

	/**
	 * Discrete to continuous dimensions for OY.
	 */
	private static double d2cY(int iy) {
		// zwr�� pozycj� w �rodku kom�rki
		return (0.5 + iy) * DY;
	}

	/**
	 * Continuous to discrete dimensions for OX.
	 */
	private static int c2dX(double x) {
		return (int) Math.round(Math.floor(x / DX));
	}

	/**
	 * Continuous to discrete dimensions for OY.
	 */
	private static int c2dY(double y) {
		return (int) Math.round(Math.floor(y / DY));
	}

}
