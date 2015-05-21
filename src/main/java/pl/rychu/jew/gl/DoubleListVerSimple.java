package pl.rychu.jew.gl;



public class DoubleListVerSimple<T> implements DoubleListVer<T> {

	private final GrowingList<T> forwardList;
	private final GrowingList<T> backwardList;

	private int version = 0;

	// ----------

	protected DoubleListVerSimple(final int arraySize) {
		forwardList = new GrowingList<>(arraySize);
		backwardList = new GrowingList<>(arraySize);
	}

	public static <T> DoubleListVerSimple<T> create(final int arraySize) {
		return new DoubleListVerSimple<T>(arraySize);
	}

	// ----------

	@Override
	public void addForward(final T value) {
		forwardList.add(value);
	}

	@Override
	public void addBackward(final T value) {
		backwardList.add(value);
	}

	@Override
	public void clear() {
		forwardList.clear();
		backwardList.clear();
		version++;
	}

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public long sizeB(final int version) throws BadVersionException {
		if (version != this.version) {
			throw new BadVersionException();
		}
		return backwardList.size();
	}

	@Override
	public long sizeF(final int version) throws BadVersionException {
		if (version != this.version) {
			throw new BadVersionException();
		}
		return forwardList.size();
	}

	@Override
	public T get(final long index, final int version) throws BadVersionException {
		if (version != this.version) {
			throw new BadVersionException();
		}

		if (index < 0) {
			return backwardList.get((-index) - 1);
		} else {
			return forwardList.get(index);
		}
	}

}
