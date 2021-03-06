package pl.rychu.jew.gl;

import java.util.ArrayList;
import java.util.List;



public class GrowingList<T> {

	private final int arraySizePower;
	private final int arraySize;
	private final int arrayOffMask;

	private final List<Object[]> listOfArrays = new ArrayList<Object[]>();

	private int currentListIndex;
	private int currentArrayIndex;

	// -----------

	protected GrowingList(final int arraySize) {
		final int power = GrowingListUtils.getPower(arraySize);

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
	}

	public void clear() {
		init();
	}

	// ----------

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

}
