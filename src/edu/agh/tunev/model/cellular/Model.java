package edu.agh.tunev.model.cellular;

import java.util.Vector;

import edu.agh.tunev.model.AbstractModel;
import edu.agh.tunev.model.Person;
import edu.agh.tunev.world.World;
import edu.agh.tunev.world.World.ProgressCallback;

public final class Model extends AbstractModel {

	public final static String MODEL_NAME = "w�sowy automat kom�rkowy";

	public Model(World world) {
		super(world);
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
			ProgressCallback callback) {
		// jakie s� rzeczywiste wymiary �wiata?
		double dimX = world.getXDimension();
		double dimY = world.getYDimension();

		// stw�rz automat (plansz� kom�rek) o obliczonych dyskretnych wymiarach;
		// u�ywa funkcji do t�umaczenia wymiar�w z ci�g�ych na dyskretne z
		// uwzgl�dnieniem DX i DY. Zobacz poni�ej ich definicje.
		board = new Board(c2dX(dimX) + 1, c2dY(dimY) + 1);

		// pozaznaczaj osoby na naszej modelowej, wewn�trznej, planszy
		for (Person p : people) {
			// w kt�rej kom�rce jest ta osoba?
			int ix = c2dX(p.getX());
			int iy = c2dY(p.getY());

			// przesu� j� dok�adnie na �rodek tej kom�rki...
			p.setPosition(d2cX(ix), d2cY(iy));
			// ... i zr�b jej tam "zdj�cie" dla interpolatora w chwili t=0[s]
			p.saveState(0.0);

			// zaznacz w odpowiedniej kom�rce automatu, �e kt�r� osob�
			Cell c = board.get(ix, iy);
			c.person = p;
		}

		// TODO: pozaznaczaj przeszkody na planszy
		
		// TODO: pozaznaczaj wyj�cia na planszy

		// kolejne iteracje automatu -- uwaga, �adnego czekania w stylu
		// Thread.sleep() -- to ma si� policzy� *jak najszybciej*! --
		// wy�wietlanie "filmu" z symulacji jest niezale�ne od obliczania (no,
		// tyle tylko zale�ne, �e mo�emy wy�wietla� tylko do momentu, kt�ry ju�
		// si� policzy�)
		int num = (int)Math.round(Math.ceil(world.getDuration() / DT));
		double t = 0;
		for (int i = 0; i < num; i++) {
			// uaktualnij rzeczywisty czas naszej symulacji
			t += DT;
			
			// przejd� do nast�pnego stanu automatu
			board.update();
			
			// por�b zdj�cia osobom w aktualnym rzeczywistym czasie
			for (Person p : people)
				p.saveState(t);
			
			// grzeczno��: zwi�ksz ProgressBar w UI
			callback.update(i + 1, num, (i + 1 == num ? "Gotowe!" : "Wci�� licz�..."));
		}
		
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
