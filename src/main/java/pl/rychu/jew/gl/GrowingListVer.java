package pl.rychu.jew.gl;



public class GrowingListVer<T> {

	private final GrowingList<T> growingList;
	private int currentVersion;

	// -----------

	protected GrowingListVer(final int arraySize) {
		growingList = GrowingList.create(arraySize);
		this.currentVersion = 0;
	}

	public static <T> GrowingListVer<T> create(final int arraySize) {
		final GrowingListVer<T> result = new GrowingListVer<>(arraySize);

		return result;
	}

	// -----------

	public void add(final T value) {
		growingList.add(value);
	}

	public void clear() {
		growingList.clear();

		currentVersion++;
	}

	// ----------

	public int getVersion() {
		return currentVersion;
	}

	public T get(final long index, final int version) throws BadVersionException {
		if (version != currentVersion) {
			throw new BadVersionException();
		}
		return growingList.get(index);
	}

	public long size(final int version) throws BadVersionException {
		if (version != currentVersion) {
			throw new BadVersionException();
		}
		return growingList.size();
	}

}
