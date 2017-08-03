package pl.rychu.jew.gl;

import java.util.ArrayList;
import java.util.List;


public class GrowingListLong {

	private final int arraySizePower;
	private final int arraySize;
	private final int arrayOffMask;

	private final List<long[]> listOfArrays = new ArrayList<long[]>();

	private int currentListIndex;
	private int currentArrayIndex;

	// -----------

	protected GrowingListLong(final int arraySize) {
		final int power = GrowingListUtils.getPower(arraySize);

		this.arraySizePower = power;
		this.arraySize = arraySize;
		this.arrayOffMask = arraySize - 1;
	}

	public static GrowingListLong create(final int arraySize) {
		final GrowingListLong result = new GrowingListLong(arraySize);

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

	public void add(final long value) {
		while (currentListIndex >= listOfArrays.size()) {
			listOfArrays.add(new long[arraySize]);
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

	public long get(long index) {
		int listIndex = (int) (index >> arraySizePower);
		int arrayIndex = (int) (index & arrayOffMask);

		return listOfArrays.get(listIndex)[arrayIndex];
	}

	public long size() {
		return ((long) arraySize) * currentListIndex + currentArrayIndex;
	}

	public long binarySearch(long key) {
		return binarySearch(0, size(), key);
	}

	private long binarySearch(long fromIndex, long toIndex, long key) {
		long low = fromIndex;
		long high = toIndex - 1;

		while (low <= high) {
			long mid = (low + high) >>> 1;
			long midVal = get(mid);

			if (midVal < key) {
				low = mid + 1;
			} else if (midVal > key) {
				high = mid - 1;
			} else {
				return mid; // key found
			}
		}
		return -(low + 1);  // key not found.
	}

}
