package main.Parsers;

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
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if ((o instanceof String)) {
			return o.equals(this.getValue());

		}
		if (!(o instanceof OptionalEntity)) {
			return false;
		}
		return this.value.equals(((OptionalEntity) o).value);
	}

	public String getValue() {
		return value;
	}
}
