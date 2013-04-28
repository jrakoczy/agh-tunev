package edu.agh.tunev.model;

/**
 * Po tej klasie musi dziedziczy� wszystko co si� rusza -- m.
 * 
 * Ale p�ki co chyba tylko model.Person. -- m.
 *
 */
public abstract class AbstractMovable {

	protected double x;
	protected double y;

	public AbstractMovable(double x, double y) {
		setPosition(x, y);
	}
	
	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
}
