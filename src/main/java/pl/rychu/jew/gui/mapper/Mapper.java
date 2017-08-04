package pl.rychu.jew.gui.mapper;

import pl.rychu.jew.gl.GrowingListLong;


public class Mapper {

	private final GrowingListLong backwardList;
	private final GrowingListLong forwardList;

	private Mapper(final int arraySize) {
		this.backwardList = GrowingListLong.create(arraySize);
		this.forwardList = GrowingListLong.create(arraySize);
	}

	public static Mapper create(final int arraySize) {
		final Mapper result = new Mapper(arraySize);

		return result;
	}

	public long size() {
		return sizeB() + sizeF();
	}

	public long sizeB() {
		return backwardList.size();
	}

	public long sizeF() {
		return forwardList.size();
	}

	public long get(final long index) {
		if (index < 0) {
			return backwardList.get((-index) - 1);
		} else {
			return forwardList.get(index);
		}
	}

	public long getInv(long root) {
		long bs = backwardList.size();
		if (!forwardList.isEmpty() && root >= forwardList.get(0)) {
			long index = forwardList.binarySearch(root);
			// -(-i-1+bs)-1 = i+1-bs-1 = i-bs
			return index >= 0 ? index + bs : index - bs;
		}
		if (bs != 0 && root <= backwardList.get(0)) {
			long index = backwardList.binarySearchInv(root);
			// -(bs-1-(-i-1))-2 = -(bs-1+i+1)-2 = -(bs+i)-2 = -bs - i - 2
			return index >= 0 ? bs - 1 - index : -bs - index - 2;
		}
		return -bs - 1;
	}

	public void clear() {
		backwardList.clear();
		forwardList.clear();
	}

	public void addF(final long value) {
		forwardList.add(value);
	}

	public void addB(final long value) {
		backwardList.add(value);
	}

}
