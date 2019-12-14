package test.state;

public interface RandomState {


	void v_EmptyPool();

	void v_ValidatingFormat1stTime();

	void v_NonEmptyPool();

	void v_ValidatingFormatGeneral();

	void v_VerifyInitialState();

	void v_CheckingRepeated();

	void e_InvalidFormat();

	void e_parseMessage();

	void e_addUrl();

	void e_DeleteAll();

	void e_RepeatedUrl();

	void e_checkRepeated();


}
