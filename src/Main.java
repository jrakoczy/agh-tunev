import java.io.FileNotFoundException;

import board.Board;

public class Main {

	/**
	 * Tymczasowo wyrzuca FileNotFoundException, p�ki nie b�dzie (o ile b�dzie!)
	 * kiedy� wybierania pliku wej�ciowego FDS-a z poziomu UI.
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Board board;

		board = new Board("firedata/tunnel.fds");

		board.initAgents(); // w jaki spos�b? gdzie inicjalni agenci? random?

		UI ui = new UI();

		for (;;) {
			board.update();
			ui.draw(board);

			// TODO: check end conditions
		}
	}

}
