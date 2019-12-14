package core.parsers;

public class OptionalEntity {
	private final String value;
	private final String definition;

	public OptionalEntity(String value, String definition) {
		this.value = value;
		this.definition = definition;
	}

	public String getDefinition() {
		return "\tCan use **" + value + "** to " + definition + "\n";
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof OptionalEntity)) {
			return false;
		}
		return this.value.equals(((OptionalEntity) o).value);
	}

	String getValue() {
		return value;
	}
}
