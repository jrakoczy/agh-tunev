package edu.agh.tunev.model;

/**
 * Ta klasa reprezentuje osob�: jej cechy niezale�ne od modelu, cechy kt�re (w
 * przysz�o�ci) mo�emy wybra� w UI dodaj�c osoby.
 * 
 * Wszystkie cechy "osobnicze", kt�re maj� sens poza konkretnym modelem, powinny
 * by� dodane do AbstractPerson. Przyk�ad:
 * 
 * Do <code>AbstractPerson</code>: wiek, rasa, orientacja, religious views
 * 
 * Do <code>Person extends AbstractPerson</code>: informacja o tym co osoba
 * s�dzi nt. konkretnych p�l automatu kom�rkowego
 */
public abstract class AbstractPerson extends AbstractMovable {

	/** Szeroko�� osoby (OX) [m] */
	protected double width = 0.5;

	/** Grubo�� osoby (OY) [m] */
	protected double girth = 0.3;

	/** Wysoko�� osoby (OZ) [m] */
	protected double height = 1.7;

	public AbstractPerson(double x, double y) {
		super(x, y);
	}

	public double getWidth() {
		return width;
	}

	public double getGirth() {
		return girth;
	}

	public double getHeight() {
		return height;
	}

}
