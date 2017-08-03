package pl.rychu.jew.gl;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created on 03.08.2017.
 */
@RunWith(JUnitParamsRunner.class)
public class GrowingListLongTest {

	@Test
	@Parameters
	public void testBinarySearch(int arraySize) {
		// given
		GrowingListLong list = new GrowingListLong(16);
		for (int i = 0; i < arraySize; i++) {
			list.add(i * 10);
		}

		// when - then
		for (int i = 0; i < arraySize; i++) {
			assertThat(list.binarySearch(i * 10)).isEqualTo(i);
			assertThat(list.binarySearch(i * 10 - 1)).isEqualTo(-(i + 1));
			assertThat(list.binarySearch(i * 10 + 1)).isEqualTo(-(i + 2));
		}
	}

	protected List<Integer> parametersForTestBinarySearch() {
		return Arrays.asList(1, 2, 3, 4, 10, 15, 16, 17, 18, 19, 31, 32, 33, 159, 160, 161);
	}

}