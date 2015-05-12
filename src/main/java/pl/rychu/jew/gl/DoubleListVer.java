package pl.rychu.jew.gl;



public class DoubleListVer<T> {

	private final GrowingList<T> forwardList;
	private final GrowingList<T> backwardList;

	private int version = 0;

	// ----------

	protected DoubleListVer(final int arraySize) {
		forwardList = new GrowingList<>(arraySize);
		backwardList = new GrowingList<>(arraySize);
	}

	public static <T> DoubleListVer<T> create(final int arraySize) {
		return new DoubleListVer<T>(arraySize);
	}

	// ----------

	public void addForward(final T value) {
		forwardList.add(value);
	}

	public void addBackward(final T value) {
		backwardList.add(value);
	}

	public void clear() {
		forwardList.clear();
		backwardList.clear();
		version++;
	}

	public int getVersion() {
		return version;
	}

	public long sizeB(final int version) throws BadVersionException {
		if (version != this.version) {
			throw new BadVersionException();
		}
		return backwardList.size();
	}

	public long sizeF(final int version) throws BadVersionException {
		if (version != this.version) {
			throw new BadVersionException();
		}
		return forwardList.size();
	}

	public T get(final long index, final int version) throws BadVersionException {
		if (index < 0) {
			return backwardList.get((-index) - 1);
		} else {
			return forwardList.get(index);
		}
	}

}
