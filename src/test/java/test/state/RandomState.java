package test.state;

interface RandomState {


	void v_EmptyPool();

	void v_ProcessingFirstMessage();

	void v_NonEmptyPool();

	void v_ProcessingMessageGeneral();

	void v_CheckingRepeated();

	void e_InvalidFormat();

	void e_sendMessage();

	void e_addUrlFromUnique();

	void e_addUrlFromValid();

	void e_DeleteAll();

	void e_RepeatedUrl();

	void e_checkRepeated();


}
