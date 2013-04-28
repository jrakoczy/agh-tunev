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

		// stw�rz automat (plansz� kom�rek) o dyskretnych wymiarach
		board = new Board((int) Math.round(Math.floor(dimX / DX)) + 1,
				(int) Math.round(Math.floor(dimY / DY)) + 1);

	}

}
