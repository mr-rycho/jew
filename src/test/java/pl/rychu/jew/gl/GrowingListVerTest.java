package pl.rychu.jew.gl;

import static org.fest.assertions.Assertions.*;
import junitparams.JUnitParamsRunner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;



@RunWith(JUnitParamsRunner.class)
public class GrowingListVerTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	// --------

	@Test
	public void shouldNotThrowOnGoodVer() throws BadVersionException {
		// given
		final GrowingListVer<String> list = GrowingListVer.create(16);
		list.add("aaa");
		final int prevVer = list.getVersion();

		// when
		final long size = list.size(prevVer);

		// then
		assertThat(size).isEqualTo(1L);
	}

	// --------

	@Test
	public void shouldThrowOnBadVer() throws BadVersionException {
		// given
		final GrowingListVer<String> list = GrowingListVer.create(16);
		list.add("aaa");
		final int prevVer = list.getVersion();
		list.clear();

		// then!!!
		expectedException.expect(BadVersionException.class);

		// when
		list.size(prevVer);
	}

}
