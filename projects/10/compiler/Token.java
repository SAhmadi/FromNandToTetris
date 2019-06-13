public class Token {
	private String type;
	private String value;

	public Token(String type, String value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;

		if (!(o instanceof Token)) return false;

		Token t = (Token) o;
		return type.equals(t.getType()) && value.equals(t.getValue());
	}

	@Override
	public String toString() {
		return "type: " + type + "; value: " + value + System.lineSeparator();
	}

	public String getType() { return type; }
	public void setType(String t) { type = t; }

	public String getValue() { return value; }
	public void setValue(String v) { value = v; }
}