package edu.agh.tunev.model.cellular;

import java.util.Vector;

import edu.agh.tunev.model.AbstractModel;
import edu.agh.tunev.model.PersonProfile;
import edu.agh.tunev.model.cellular.agent.NotANeighbourException;
import edu.agh.tunev.model.cellular.agent.Person;
import edu.agh.tunev.model.cellular.agent.WrongOrientationException;
import edu.agh.tunev.model.cellular.grid.Board;
import edu.agh.tunev.model.cellular.grid.Cell;
import edu.agh.tunev.statistics.LifeStatistics;
import edu.agh.tunev.statistics.Statistics.AddCallback;
import edu.agh.tunev.world.World;
import edu.agh.tunev.world.World.ProgressCallback;

public final class Model extends AbstractModel {

	public final static String MODEL_NAME = "Social Distances Cellular Automata";
	private final static double INTERSECTION_TOLERANCE = 0.2;

	public Model(World world) {
		super(world);
	}

	private static final double DT = 0.05;

	private Board board;
	private AllowedConfigs allowedConfigs;

	@Override
	public void simulate(double duration, Vector<PersonProfile> profiles,
			ProgressCallback progressCallback, AddCallback addCallback) {
		// pokaĹĽ info o inicjalizacji w ui, bo trwa zanim zacznie iterowaÄ‡ i nie
		// wiadomo ocb :b
		int num = (int) Math.round(Math.ceil(world.getDuration() / DT));
		progressCallback.update(0, num, "Initializing...");

		// TODO: pododawaj jakieĹ› wykresy do UI zwiÄ…zane z tym modelem
		//
		// sidenote: zobacz helpa do interfejsu Statistics: gdy dany wykres
		// pasuje do wielu modeli (np. liczba zabitych jako f(t)), to dodaj jego
		// klasÄ™ do pakietu tunev.statistics; jeĹ›li pasuje tylko do tego modelu,
		// to dodaj do pakietu tego modelu
		LifeStatistics lifeStatistics = new LifeStatistics();
		addCallback.add(lifeStatistics);
		// minor fix: przeniosĹ‚em wykresy przed tworzenie automatu, ĹĽeby juĹĽ
		// byĹ‚y dostÄ™pne do otwarcia na etapie inicjalizacji

		// stwĂłrz automat (planszÄ™ komĂłrek)
		board = new Board(world);

		// TODO: exception handling
		try {
			allowedConfigs = new AllowedConfigs(PersonProfile.WIDTH,
					PersonProfile.GIRTH, Cell.CELL_SIZE, INTERSECTION_TOLERANCE);
		} catch (NeighbourIndexException | WrongOrientationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// stwĂłrz sobie swoje reprezentacje ludzi:
		Vector<Person> people = new Vector<Person>();
		for (PersonProfile profile : profiles)
			try {
				people.add(new Person(profile, board.getCellAt(Cell
						.c2d(profile.initialPosition)), allowedConfigs));
			} catch (WrongOrientationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		// TODO: pozaznaczaj przeszkody na planszy

		// TODO: pozaznaczaj wyjĹ›cia na planszy

		// kolejne iteracje automatu -- uwaga, ĹĽadnego czekania w stylu
		// Thread.sleep() -- to ma siÄ™ policzyÄ‡ *jak najszybciej*! --
		// wyĹ›wietlanie "filmu" z symulacji jest niezaleĹĽne od obliczania (no,
		// tyle tylko zaleĹĽne, ĹĽe moĹĽemy wyĹ›wietlaÄ‡ tylko do momentu, ktĂłry juĹĽ
		// siÄ™ policzyĹ‚)
		double t = 0;
		for (int iteration = 1; iteration <= num; iteration++) {
			// uaktualnij rzeczywisty czas naszej symulacji
			t += DT;

			board.update(t);

			// porĂłb zdjÄ™cia osobom w aktualnym rzeczywistym czasie
			for (Person p : people) {
				try {
					try {
						p.update();
					} catch (NotANeighbourException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (NeighbourIndexException | WrongOrientationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				interpolator.saveState(p.profile, t, p.getCurrentState());
			}

			// TODO: uaktualnij wykresy, ktĂłre mogÄ… byÄ‡ aktualizowane w trakcie
			// symulowania
			int currentNumDead = 123; // prawdopodobnie ta dana ustawiana
										// gdzie indziej ;p~
			int currentNumAlive = 12;
			int currentNumRescued = 72;
			lifeStatistics.add(t, currentNumAlive, currentNumRescued, currentNumDead);

			// grzecznoĹ›Ä‡: zwiÄ™ksz ProgressBar w UI
			progressCallback.update(iteration, num,
					(iteration < num ? "Simulating..." : "Done."));
		}

		// TODO: ew. wypeĹ‚nij wykresy, ktĂłre mogÄ… byÄ‡ wypeĹ‚nione dopiero po
		// zakoĹ„czeniu caĹ‚ej symulacji

		// i tyle ^_^
	}

}
