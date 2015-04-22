package pl.rychu.jew.gl;

import static junitparams.JUnitParamsRunner.$;
import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

import pl.rychu.jew.gl.GrowingList;

@RunWith(JUnitParamsRunner.class)
public class GrowingListTest {

	@Test
	@Parameters
	public void shouldRetainElements(final int sizeToInsert) {
		// given
		final GrowingList<String> growingList = new GrowingList<String>(32);

		// when
		for (int i=0; i<sizeToInsert; i++) {
			final String str = ""+((char)48+i);
			growingList.add(str);
		}

		// then
		final int ver = 0;
		assertThat(growingList.size(ver)).isEqualTo(sizeToInsert);
		for (int i=0; i<sizeToInsert; i++) {
			final String str = ""+((char)48+i);
			assertThat(growingList.get(i, ver)).isEqualTo(str);
		}
	}

	protected List<Object[]> parametersForShouldRetainElements() {
		final int size = 100;
		List<Object[]> result = new ArrayList<Object[]>(size);

		for (int i=0; i<size; i++) {
			result.add($(i));
		}

		return result;
	}

	// ------------------------

	@Test
	public void shouldNotAllowIncorrectSizes() {
		final Set<Integer> powersOfTwo = getPowersOfTwo();

		for (int i=0; i<100000; i++) {
			assertThat(allowsCreatingGrowingList(i))
			 .isEqualTo(powersOfTwo.contains(i));
		}
	}

	private static Set<Integer> getPowersOfTwo() {
		final Set<Integer> result = new HashSet<Integer>();

		for (int i=0; i<31; i++) {
			result.add(1 << i);
		}

		return result;
	}

	private boolean allowsCreatingGrowingList(final int size) {
		try {
			new GrowingList<String>(size);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
