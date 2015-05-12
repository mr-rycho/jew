package pl.rychu.jew.gl;

import static org.fest.assertions.Assertions.*;
import static junitparams.JUnitParamsRunner.$;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;



@RunWith(JUnitParamsRunner.class)
public class DoubleListVerTest {

	@Test
	@Parameters
	public void shouldReturnCorrectSize(
	 final Collection<InsertEvent<Integer>> insertEvents
	 , final long expectedSizeB, final long expectedSizeF)
	 throws BadVersionException {
		// given
		final DoubleListVer<Integer> doubleListVer = DoubleListVer.create(4);

		// when
		applyEvents(doubleListVer, insertEvents);

		// then
		final int version = doubleListVer.getVersion();
		assertThat(doubleListVer.sizeB(version)).isEqualTo(expectedSizeB);
		assertThat(doubleListVer.sizeF(version)).isEqualTo(expectedSizeF);
	}

	protected List<Object[]> 	parametersForShouldReturnCorrectSize() {
		final List<Object[]> result = new ArrayList<>();

		result.add($(Arrays.asList(f(1), f(2), f(3), f(4), f(5)), 0, 5));
		result.add($(Arrays.asList(f(1), f(2), f(3), f(4), b(5)), 1, 4));
		result.add($(Arrays.asList(b(1), f(2), f(3), f(4), f(5)), 1, 4));
		result.add($(Arrays.asList(f(1), f(2), b(3), f(4), f(5)), 1, 4));
		result.add($(Arrays.asList(b(1), b(2), b(3), b(4), b(5)), 5, 0));

		return result;
	}

	// ----------

	@Test
	@Parameters
	public void shouldReturnCorrectElementsDespiteInsertOrder(final long seed)
	 throws BadVersionException {
		// given
		final DoubleListVer<Integer> doubleListVer = DoubleListVer.create(4);
		final Random random = new Random(seed);
		final List<Collection<Integer>> evts = getInsertEvents(random.nextLong());

		// when
		addValuesToDoubleListVerRandomly(doubleListVer, evts.get(0), evts.get(1)
		 , random.nextLong());

		// then
		final int ver = doubleListVer.getVersion();
		final long sizeB = doubleListVer.sizeB(ver);
		for (long i=-sizeB; i<-sizeB+10; i++) {
			assertThat(doubleListVer.get(i, ver)).isEqualTo((int)(i+sizeB));
		}
	}

	protected List<Object[]> parametersForShouldReturnCorrectElementsDespiteInsertOrder() {
		final int count = 100;
		final List<Object[]> result = new ArrayList<>(count);
		final Random random = new Random();
		for (int i=0; i<count; i++) {
			result.add($(random.nextLong()));
		}
		return result;
	}

	private List<Collection<Integer>> getInsertEvents(final long seed) {
		final Random random = new Random(seed);
		final int offset = random.nextInt(10);
		final Collection<Integer> listB = new ArrayList<>();
		final Collection<Integer> listF = new ArrayList<>();
		for (int i=offset-1; i>=0; i--) {
			listB.add(i);
		}
		for (int i=offset; i<10; i++) {
			listF.add(i);
		}
		final List<Collection<Integer>> result = new ArrayList<>();
		result.add(listB);
		result.add(listF);
		return result;
	}

	private void addValuesToDoubleListVerRandomly(final DoubleListVer<Integer> doubleListVer
	 , final Collection<Integer> valsB, final Collection<Integer> valsF
	 , final long seed) {
		final Random random = new Random(seed);
		final Iterator<Integer> iterB = valsB.iterator();
		final Iterator<Integer> iterF = valsF.iterator();
		while (iterB.hasNext() || iterF.hasNext()) {
			if (random.nextBoolean()) {
				if (iterB.hasNext()) {
					final int value = iterB.next();
					doubleListVer.addBackward(value);
				}
			} else {
				if (iterF.hasNext()) {
					final int value = iterF.next();
					doubleListVer.addForward(value);
				}
			}
		}
	}

	// -------------------------------

	private <T> void applyEvents(final DoubleListVer<T> doubleListVer
	 , final Collection<InsertEvent<T>> events) {
		for (final InsertEvent<T> event: events) {
			if (event.isForward()) {
				doubleListVer.addForward(event.getValue());
			} else {
				doubleListVer.addBackward(event.getValue());
			}
		}
	}

	private static <T> InsertEvent<T> f(final T value) {
		return new InsertEvent<T>(value, true);
	}

	private static <T> InsertEvent<T> b(final T value) {
		return new InsertEvent<T>(value, false);
	}

	// ==============

	private static class InsertEvent<T> {
		private final T value;
		private final boolean forward;

		public InsertEvent(final T value, final boolean forward) {
			super();
			this.value = value;
			this.forward = forward;
		}

		public T getValue() {
			return value;
		}

		public boolean isForward() {
			return forward;
		}
	}
}
