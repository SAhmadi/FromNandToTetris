/**
 * Represents a token,
 * with type and value
 * 
 * @author Sirat Ahmadi
 * @version 1.0
 */
public class Token {
	private String type;
	private String value;

	/**
	 * Init token type and value
	 * 
	 * @param type The token type (e.g. keyword, symbol, ...)
	 * @param value The token value (e.g. class, method, let, ...)
	 */
	public Token(String type, String value) {
		this.type = type;
		this.value = value;
	}


	/**
	 * Compare current token with other token
	 * 
	 * @param o The Token-Object to compare to
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;

		if (!(o instanceof Token)) return false;

		Token t = (Token) o;
		return type.equals(t.getType()) && value.equals(t.getValue());
	}


	/**
	 * String representation of the current token
	 * 
	 * @return String representation of current object 
	 */
	@Override
	public String toString() {
		return String.format("type = %s | value = %s%s", type, value, System.lineSeparator());
	}

	
	/* GETTERS and SETTERS */
	public String getType() { return type; }
	public void setType(String t) { type = t; }

	public String getValue() { return value; }
	public void setValue(String v) { value = v; }
}