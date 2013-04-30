package edu.agh.tunev.model;

import java.util.Vector;

import edu.agh.tunev.interpolation.Interpolator;
import edu.agh.tunev.world.World;

/**
 * Po tym dziedziczy klasa g��wna ka�dego modelu. -- m.
 * 
 * �eby "zarejestrowa�" nowy model, �eby by� widoczny w UI, trzeba doda� linijk�
 * z nazw� jego klasy do <code>edu.agh.tunev.Main.main()</code>.
 * 
 * @param <T>
 *            m�wi o tym, kt�ra klasa reprezentuje osob� w danym modelu (musi
 *            dziedziczy� po AbstractPerson).
 */
public abstract class AbstractModel<T extends AbstractPerson> {

	final protected World world;
	final protected Interpolator interpolator;

	/**
	 * Nazwa modelu w UI. Jak to nie b�dzie ustawione w klasie dziedzicz�cej, to
	 * register() w main() rzuci wyj�tek.
	 */
	public static String MODEL_NAME;

	public AbstractModel(World world, Interpolator interpolator) {
		this.world = world;
		this.interpolator = interpolator;
	}

	/**
	 * Metoda startuj�ca symulacj�.
	 * 
	 * @param duration
	 *            Czas trwania symulacji.
	 * @param people
	 *            Lista os�b w danym �wiecie.
	 * @param callback
	 *            Wywo�ujemy po ka�dej iteracji
	 *            <code>callback.update(done, total,
	 *            msg</code>), gdzie <code>done</code> to numer aktualnej
	 *            iteracji, a <code>total</code> to liczba wszystkich
	 *            zaplanowanych, a <code>msg</code> to jaki� komunikat tekstowy,
	 *            mo�e by� <code>""</code>/<code>null</code>. Po to, �eby
	 *            rysowa� ProgressBar ile ju� si� policzy�o z ca�o�ci.
	 */
	public abstract void simulate(double duration, Vector<T> people,
			World.ProgressCallback callback);

	/** tego nie ruszamy :] t�umaczy Vector<AbstractPerson> -> Vector<T> */
	@SuppressWarnings("unchecked")
	public final void simulateWrapper(double duration,
			Vector<AbstractPerson> people, World.ProgressCallback callback) {
		Vector<T> castedPeople = new Vector<T>();
		for (AbstractPerson p : people)
			castedPeople.add((T) p);
		simulate(duration, castedPeople, callback);
	}
}
