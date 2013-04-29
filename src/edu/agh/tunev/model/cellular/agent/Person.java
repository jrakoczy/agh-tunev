package edu.agh.tunev.model.cellular.agent;

import edu.agh.tunev.model.AbstractPerson;

/**
 * Tutaj mo�esz pami�ta� sobie jakie� dane o osobie, kt�re maj� znaczenie tylko
 * w implementowanym modelu. Wszystkie cechy "osobnicze", kt�re maj� sens poza
 * konkretnym modelem, powinny by� dodane do AbstractPerson. Przyk�ad:
 * 
 * Do <code>AbstractPerson</code>: wiek, rasa, orientacja, religious views
 * 
 * Do <code>Person extends AbstractPerson</code>: informacja o tym co osoba
 * s�dzi nt. konkretnych p�l automatu kom�rkowego
 * 
 */
public final class Person extends AbstractPerson {

	public Person(double x, double y) {
		super(x, y);
		// TODO Auto-generated constructor stub
	}

}
