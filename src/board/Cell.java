package board;public class Cell {	private Type type;	/**	 * Typ kom�rki.	 * 	 * Raczej nie u�ywajmy na razie {@link #NORMAL}, bo nie wiem co to by mog�o	 * znaczy�... Dlatego zrobi�em typ, a nie flag� blocked, bo nie wiadomo czy	 * si� jeszcze nie przyda, a pewnie tak!	 * 	 * @author Michal	 * 	 */	public enum Type {		NORMAL, BLOCKED	}	public Cell() {		type = Type.NORMAL;	}	public void setType(Type type) {		this.type = type;	}

	public Type getType() {
		return type;
	}	// TODO: do modyfikacji	public float getTemperature() {		return 0;	}	// TODO: do modyfikacji	public float getCOConcentration() {		return 0;	}		//TODO: metoda sprawdzajaca, czy w danej komorce znajduje sie przeszkoda	public boolean isOccupied(){		return false;	}}