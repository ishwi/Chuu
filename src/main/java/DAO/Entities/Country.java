package DAO.Entities;

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
}
