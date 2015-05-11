package pl.rychu.jew.gl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GrowingListVerLocked<T> extends GrowingListVer<T> {

	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

	// ---------------

	protected GrowingListVerLocked(final int arraySize) {
		super(arraySize);
	}

	public static <T> GrowingListVerLocked<T> create(final int arraySize) {
		final GrowingListVerLocked<T> result = new GrowingListVerLocked<T>(arraySize);

		return result;
	}

	// ---------------

	public void add(final T value) {
		final Lock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			super.add(value);
		} finally {
			writeLock.unlock();
		}
	}

	public void clear() {
		final Lock writeLock = rwLock.writeLock();
		writeLock.lock();
		try {
			super.clear();
		} finally {
			writeLock.unlock();
		}
	}

	// ----------

	public T get(final long index, final int version) throws BadVersionException {
		final Lock readLock = rwLock.readLock();
		readLock.lock();
		try {
			return super.get(index, version);
		} finally {
			readLock.unlock();
		}
	}

	public long size(final int version) throws BadVersionException {
		final Lock readLock = rwLock.readLock();
		readLock.lock();
		try {
			return super.size(version);
		} finally {
			readLock.unlock();
		}
	}

}
