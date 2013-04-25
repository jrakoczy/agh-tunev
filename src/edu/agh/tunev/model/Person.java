package edu.agh.tunev.model;

import edu.agh.tunev.world.World;

/**
 * Ta klasa reprezentuje osob�. Cechy osoby nie zale�� od modelu, dlatego ka�dy
 * model u�ywa takiej samej osoby. Dlatego final. Nie dziedziczymy. -- m.
 * 
 */
public final class Person extends AbstractMovable {
	
	/** Szeroko�� osoby (OX) [m] */
	private double width = 0.5;

	/** Grubo�� osoby (OY) [m] */
	private double girth = 0.3;

	/** Wysoko�� osoby (OZ) [m] */
	private double height = 1.7;

	public Person(World world, double x, double y) {
		super(world);
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
