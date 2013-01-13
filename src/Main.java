import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Date;

import board.Board;
import board.Board.Exit;
import board.Board.NoPhysicsDataException;

public final class Main {

	private static final double SIMULATION_SPEEDUP = 1.0f; // ... times

	/**
	 * Tymczasowo wyrzuca FileNotFoundException, p�ki nie b�dzie (o ile b�dzie!)
	 * kiedy� wybierania pliku wej�ciowego FDS-a z poziomu UI.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws FileNotFoundException,
			ParseException {

		Board board;
		board = new Board();

		FDSParser parser = new FDSParser(board, "data/");

		board.initAgents(60);
		board.initAgentsRandomly(300);

		for (Exit x : board.getExits())
			System.out.println("Wyjscie X: " + x.getExitX() + "Wyjscie Y: "
					+ x.getExitY());

		UI ui = new UI();

		// all time values in [ms]
		double simulationDuration = parser.getDuration();
		double simulationTime = 0;
		double dt;
		long currentCPUTime = new Date().getTime();
		long previousCPUTime;

		while (simulationTime < simulationDuration) {
			try {
				parser.readData(simulationTime);
			} catch (FileNotFoundException | ParseException e) {
				/*
				 * if user deleted a needed data file *during* simulation, then
				 * they *won't have* that data -,- ignore, keep simulating
				 */
			}

			previousCPUTime = currentCPUTime;
			currentCPUTime = new Date().getTime();
			dt = (currentCPUTime - previousCPUTime) * SIMULATION_SPEEDUP;
			simulationTime += dt;

			try {
				board.update(dt);

			} catch (NoPhysicsDataException e1) {
				// Brak danych na planszy
				e1.printStackTrace();
			}
			ui.draw(board);

			// sztuczne op�nienie, tylko na razie -- m.
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
	}

}
