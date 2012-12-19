package board;import agent.Agent;public class Cell {	private Type type;	private int x, y;	private Agent agent;	private float temperature = 0.0f, coConcentration = 0.0f;	/**	 * Typ kom�rki.	 * 	 * Raczej nie u�ywajmy na razie {@link #EMPTY}, bo nie wiem co to by mog�o	 * znaczy�... Dlatego zrobi�em typ, a nie flag� blocked, bo nie wiadomo czy	 * si� jeszcze nie przyda, a pewnie tak!	 * 	 * @author Michal	 * 	 */	public enum Type {		EMPTY, BLOCKED	}	public Cell(int x, int y) {		type = Type.EMPTY;		this.x = x;		this.y = y;	}	public void setType(Type type) {		this.type = type;	}	public Type getType() {		return type;	}	public int getX() {		return x;	}	public int getY() {		return y;	}	public float getTemperature() {		return temperature;	}	public float getCOConcentration() {		return coConcentration;	}	public void removeAgent() {		agent = null;	}	public void addAgent(Agent agent) {		this.agent = agent;	}	public Agent getAgent() {		return agent;	}	/**	 * Czy kom�rka jest do wzi�cia? Jednak to ma sens, przepraszam. :]	 * 	 * @return {@link true} je�li kom�rka jest pusta i nie ma na niej Agenta.	 */	public boolean isOccupied() {		return !(type == Type.EMPTY && agent == null);	}}