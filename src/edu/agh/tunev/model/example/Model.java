package edu.agh.tunev.model.example;

import java.util.Vector;

import edu.agh.tunev.interpolation.Interpolator;
import edu.agh.tunev.model.AbstractModel;
import edu.agh.tunev.model.AbstractPerson;
import edu.agh.tunev.world.World;
import edu.agh.tunev.world.World.ProgressCallback;

// zauwa�, �e rozszerzaj�c, podajemy w parametrze klas�, kt�ra modeluje nam
// osob� w tym modelu
public final class Model extends AbstractModel<Person> {

	// nazwa pod jak� nasz model jest widoczny w UI; obecna prawdopodobnie do
	// zmiany
	//
	// �eby "zarejestrowa�" nowy model, �eby by� widoczny w UI, trzeba doda�
	// linijk� z nazw� jego klasy do <code>edu.agh.tunev.Main.main()</code>.
	public final static String MODEL_NAME = "model przyk�adowy";

	public Model(World world, Interpolator interpolator) {
		super(world, interpolator);
	}

	@Override
	public void simulate(double duration, Vector<Person> people,
			ProgressCallback callback) {
		// TODO Auto-generated method stub

		// zobacz helpa do rodzica (AbstractModel.simulate) -- po prostu
		// potrzymaj myszk� nad nazw� metody 4 linijki wy�ej =)

		// przyk�adzik:

		final double dt = 0.01;
		final int num = (int) Math.round(duration / dt);
		double t = 0.0;
		for (int i = 1; i <= num; i++) {
			for (AbstractPerson p : people) {
				// ruszamy ludzikiem, update'ujemy .x i .y

				// ... cokolwiek

				// wymiary �wiata to World.getXDimension() i Y accordingly

				// przeszkody to World.getObstacles, ale tego jeszcze nie ma

				// wa�ne: zapisujemy jej stan w danej chwili t w interpolatorze
				// -- niezwykle ta� czynno�� istotna!
				interpolator.saveState(p, t);
			}

			// wa�ne: i czas podawany w .saveState(t) i wsp�rz�dne .x i .y s�
			// rzeczywiste (czyli sekundy i metry!)

			// zwi�kszamy nasz wewn�trzny czas symulatora
			t += dt;

			// zwi�kszamy ProgressBar w UI, �eby user nie my�la�, �e nic si� nie
			// dzieje =)~
			callback.update(i, num, "Simulating...");

			// a jakie jeszcze klasy s� w tym pakiecie, czy to implementuj�ce
			// automat kom�rkowy itd. -- dla mnie nieistotne. Te� ofc ta funkcja
			// nie musi tak wygl�da�. Dla mnie jest wa�ne, �ebym dosta�
			// .saveState(t) na ka�dej osobie w odpowiednich dyskretnych czasach
			// i tyle

			// mo�emy um�wi� si�, �e jak Person znika z planszy, to przypisujemy
			// do .x i .y warto�� Double.NaN

			// warto te� wywo�a� callback.update() okresowo, �eby pokaza� post�p
			// oblicze�

			// to tyle :B powodzenia, ja zabieram si� za drug� stron�

			// 05:09, ja piernicz� -,-
		}
	}

}
