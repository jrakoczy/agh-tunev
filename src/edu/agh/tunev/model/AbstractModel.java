package edu.agh.tunev.model;

import java.util.Vector;

import edu.agh.tunev.world.World;

/**
 * Po tym dziedziczy klasa g��wna ka�dego modelu. -- m.
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
	 *            Wywo�ujemy po ka�dej iteracji callback.update(done, total,
	 *            msg), gdzie done to numer aktualnej iteracji, a total to
	 *            liczba wszystkich zaplanowanych, a msg to jaki� komunikat
	 *            tekstowy, mo�e by� ""/null. Po to, �eby rysowa� ProgressBar
	 *            ile ju� si� policzy�o z ca�o�ci.
	 */
	public abstract void simulate(double duration, Vector<Person> people,
			World.ProgressCallback callback);

}
