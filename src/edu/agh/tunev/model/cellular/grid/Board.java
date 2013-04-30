package edu.agh.tunev.model.cellular.grid;

import java.util.Vector;

public class Board {

	private Vector<Vector<Cell>> cells;

	public Board(int nx, int ny) {
		cells = new Vector<Vector<Cell>>();

		for (int iy = 0; iy < ny; iy++) {
			Vector<Cell> row = new Vector<Cell>();
			for (int ix = 0; ix < nx; ix++)
				row.add(new Cell());
			cells.add(row);
		}
	}

	public Cell get(int ix, int iy) {
		return cells.get(iy).get(ix);
	}

	public void update() {
		// TODO Auto-generated method stub

		// uwaga -- w funkcji przej�cia, gdy zmieniasz kom�rk� osoby,
		// pami�taj, �eby w samej osobie uaktualni� jej rzeczywist� pozycj�:
		// Person.setPosition(x,y) -- ale (x,y) s� rzeczywiste, wi�c musisz u�y�
		// Model.d2cX() i Model.d2cY() =)
		//
		// a potem, �eby przeiterowa� po osobach i zrobi�
		// interpolator.saveState, ale to ju� chyba w Model.simulate, tak jak
		// jest teraz?
	}

}
