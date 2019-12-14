package test.state;

import test.commands.utils.TestResources;
import org.graphwalker.core.machine.ExecutionContext;
import org.junit.ClassRule;


public class RandomStateTest extends ExecutionContext implements RandomState {
	@ClassRule
	public static final TestResources res = new TestResources();

	@Override
	public void v_EmptyPool() {
		System.out.println("Running: v_EmptyPool");

	}

	@Override
	public void v_ValidatingFormat1stTime() {
		System.out.println("Running: v_ValidatingFormat1stTime");

	}

	@Override
	public void v_NonEmptyPool() {
		System.out.println("Running: v_NonEmptyPool");

	}

	@Override
	public void v_ValidatingFormatGeneral() {
		System.out.println("Running: v_ValidatingFormatGeneral");

	}

	@Override
	public void v_VerifyInitialState() {
		System.out.println("Running: v_VerifyInitialState");

	}

	@Override
	public void v_CheckingRepeated() {
		System.out.println("Running: v_CheckingRepeated");

	}

	@Override
	public void e_InvalidFormat() {
		System.out.println("Running: e_InvalidFormat");

	}

	@Override
	public void e_parseMessage() {
		System.out.println("Running: e_parseMessage");

	}

	@Override
	public void e_addUrl() {
		System.out.println("Running: e_addUrl");

	}

	@Override
	public void e_DeleteAll() {
		System.out.println("Running: e_DeleteAll");

	}

	@Override
	public void e_RepeatedUrl() {
		System.out.println("Running: e_RepeatedUrl");

	}

	@Override
	public void e_checkRepeated() {
		System.out.println("Running: e_checkRepeated");

	}
}
