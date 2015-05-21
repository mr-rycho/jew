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
