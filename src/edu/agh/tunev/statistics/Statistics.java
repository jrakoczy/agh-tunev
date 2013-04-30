package edu.agh.tunev.statistics;

import org.jfree.chart.JFreeChart;

/**
 * Interfejs, kt�ry implementuj� obiekty reprezentuj�ce jakie� zmierzone dane w
 * modelu.
 * 
 * Dla danych maj�cych sens dla ka�dego modelu (np. liczba zabitych w czasie),
 * umieszczajmy ich klas� w tym pakiecie.
 * 
 * Dla danych maj�cych sens tylko dla konkretnego modelu, umieszczajmy ich klas�
 * gdzie� w pakiecie tego modelu. Mo�e edu.agh.tunev.model._nazwa_.statistics?
 * 
 */
public interface Statistics {

	public abstract String getTitle();

	public abstract JFreeChart getChart();

	/**
	 * Kiedy chcemy, �eby konkretne Statistics by�o dost�pne w UI, musimy wywo�a�
	 * Statistics.AddCallback.add(konkretne_statistics). AddCallback jest
	 * przekazywany w parametrze do AbstractModel.simulate(), podobnie jak
	 * ProgressCallback.
	 */
	public interface AddCallback {
		public void add(Statistics statistics);
	}

}
