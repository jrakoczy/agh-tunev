package edu.agh.tunev.model;

import java.util.Vector;

import edu.agh.tunev.world.World;

/**
 * Po tym dziedziczy klasa g��wna ka�dego modelu. -- m.
 * 
 * �eby "zarejestrowa�" nowy model, �eby by� widoczny w UI, trzeba doda� linijk�
 * z nazw� jego klasy do <code>edu.agh.tunev.Main.main()</code>.
 * 
 */
public abstract class AbstractModel {

	final protected World world;

	/**
	 * Nazwa modelu w UI.
	 */
	public static String MODEL_NAME;

	public AbstractModel(World world) {
		this.world = world;
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
	public abstract void simulate(double duration, Vector<Person> people,
			World.ProgressCallback callback);

}
