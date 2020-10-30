package dao.entities;

import java.util.Objects;

public class Country {
	private final String countryName;
	private final String countryCode;

	public Country(String countryName, String countryCode) {
		this.countryName = countryName;
		this.countryCode = countryCode;

	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getCountryName() {
		return countryName;
	}

	@Override
	public int hashCode() {
		int result = countryName != null ? countryName.hashCode() : 0;
		result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Country country = (Country) o;

		if (!Objects.equals(countryName, country.countryName)) return false;
		return Objects.equals(countryCode, country.countryCode);
	}
}
