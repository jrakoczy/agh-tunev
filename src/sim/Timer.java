package sim;

/** Klasa mierz�ca czas symulacji, wszystkie warto�ci w [ms] */
public class Timer {
	/** Przyspieszenie symulacji wzg czasu w pliku z FDS */
	private static final double SIMULATION_SPEEDUP = 1.0f;

	/** Pocz�tek symulacji mierzony BEZWZGL�DNIE wg czasu systemu */
	private long start_time;

	/** Aktualny czas symulacji, warto�� WZGL�DNA w odniesieniu do start_time */
	private long current_time;

	/** R�nica czasu pomi�dzy aktualnym current_dt, a poprzednim */
	private double current_dt;

	/** Metoda zaczyna odmierzanie czasu */
	public void init() {
		start_time = System.currentTimeMillis();
		current_time = 0;
		current_dt = 0;
	}

	/** Aktualizujemy timer wzg czasu systemu */
	public void updateTime() {
		long new_time = System.currentTimeMillis() - start_time;
		current_dt = (new_time - current_time) * SIMULATION_SPEEDUP;
		current_time = new_time;
	}

	/**
	 * @return aktualny wzgl�dny czas symulacji
	 */
	public long getCurrentTime() {
		return current_time;
	}

	/**
	 * @return aktualn� r�niczk� czasu
	 */

	public double getCurrentDt() {
		return current_dt;
	}
}
