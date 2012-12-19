import java.io.FileNotFoundException;

import board.Board;

public class Main {

	private static final int ITERATION_DURATION = 500; // [ms]
	private static final float SIMULATION_SPEEDUP = 1.0f; // ... times

	/**
	 * Tymczasowo wyrzuca FileNotFoundException, p�ki nie b�dzie (o ile b�dzie!)
	 * kiedy� wybierania pliku wej�ciowego FDS-a z poziomu UI.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Board board;

		board = new Board();

		FDSParser parser = new FDSParser(board);

		int duration = parser.inputFile("firedata/tunnel.fds");
		parser.setDataDirectory("firedata/data");
		
		int currentTime = 0; // [ms]

		board.initAgentsRandomly(20);

		UI ui = new UI();

		while (currentTime < duration) {
			parser.readData(currentTime);

			board.update();
			ui.draw(board);

			try {
				Thread.sleep(Math
						.round(ITERATION_DURATION * SIMULATION_SPEEDUP));
			} catch (InterruptedException e) {
			}
			currentTime += ITERATION_DURATION;
		}
	}

}
