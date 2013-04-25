package edu.agh.tunev.model.cellular;

import java.util.Vector;

import edu.agh.tunev.model.AbstractModel;
import edu.agh.tunev.model.Person;
import edu.agh.tunev.world.World;
import edu.agh.tunev.world.World.ProgressCallback;

public class Model extends AbstractModel {

	public Model(World world) {
		super(world);
	}

	@Override
	public void simulate(double duration, Vector<Person> people,
			ProgressCallback callback) {
		// TODO Auto-generated method stub

		// przyk�adzik:

		final double dt = 0.01;
		final int num = (int) Math.round(duration / dt);
		double t = 0.0;
		for (int i = 1; i <= num; i++) {
			for (Person p : people) {
				// ruszamy ludzikiem, update'ujemy .x i .y

				// ... cokolwiek

				// wymiary �wiata to World.getXDimension() i Y accordingly

				// przeszkody to World.getObstacles, ale tego jeszcze nie ma

				// wa�ne: zapisujemy jej stan w danej chwili t w interpolatorze
				// -- niezwykle ta� czynno�� istotna!
				p.saveState(t);
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
