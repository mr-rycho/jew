package pl.rychu.jew.gl;



public interface DoubleListVer<T> {

	void addForward(final T value);

	void addBackward(final T value);

	void clear();

	int getVersion();

	long sizeB(final int version) throws BadVersionException;

	long sizeF(final int version) throws BadVersionException;

	T get(final long index, final int version) throws BadVersionException;

}
