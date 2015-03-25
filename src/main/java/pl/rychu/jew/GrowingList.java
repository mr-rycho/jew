package pl.rychu.jew;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GrowingList<T> {

	private final int arraySizePower;
	private final int arraySize;
	private final int arrayOffMask;

	private final List<Object[]> listOfArrays = new ArrayList<Object[]>();

	private int currentListIndex;
	private int currentArrayIndex;

	private final CopyOnWriteArrayList<IndexListener> listeners
	 = new CopyOnWriteArrayList<>();

	// -----------

	protected GrowingList(final int arraySize) {
		final int power = getPower(arraySize);

		this.arraySizePower = power;
		this.arraySize = arraySize;
		this.arrayOffMask = arraySize - 1;
	}

	public static <T> GrowingList<T> create(final int arraySize) {
		final GrowingList<T> result = new GrowingList<>(arraySize);

		result.init();

		return result;
	}

	// -----------

	protected void init() {
		listOfArrays.clear();
		currentListIndex = 0;
		currentArrayIndex = 0;
	}

	// -----------

	public void add(final T value) {
		while (currentListIndex >= listOfArrays.size()) {
			listOfArrays.add(new Object[arraySize]);
			currentArrayIndex = 0;
		}

		listOfArrays.get(currentListIndex)[currentArrayIndex] = value;

		currentArrayIndex++;

		if (currentArrayIndex >= arraySize) {
			currentArrayIndex = 0;
			currentListIndex++;
		}

		for (final IndexListener li: listeners) {
			li.lineAdded();
		}
	}

	public void clear() {
		init();

		for (final IndexListener li: listeners) {
			li.indexWasReset();
		}
	}

	public T get(final long index) {
		final int listIndex = (int)(index >> arraySizePower);
		final int arrayIndex = (int)(index & arrayOffMask);

		@SuppressWarnings("unchecked")
		final T result = (T)listOfArrays.get(listIndex)[arrayIndex];
		return result;
	}

	public long size() {
		return ((long)arraySize) * currentListIndex + currentArrayIndex;
	}

	// -----------

	public void addListener(final IndexListener l) {
		listeners.add(l);
	}

	public void removeListener(final IndexListener l) {
		listeners.remove(l);
	}

	// -----------

	private static final int getPower(final int num) {
		if (num <= 0) {
			throw new IllegalArgumentException("bad size: "+num);
		}
		int power = 0;
		int n = num;
		while (true) {
			if ((n & 1) == 1) {
				if (n != 1) {
					throw new IllegalArgumentException("num must be power of 2");
				} else {
					return power;
				}
			}
			n >>>= 1;
			power++;
		}
	}

}
