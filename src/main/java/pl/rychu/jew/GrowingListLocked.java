package pl.rychu.jew;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GrowingListLocked<T> extends GrowingList<T> {

	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

	// ---------------

	public GrowingListLocked(int arraySize) {
		super(arraySize);
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

	public T get(final long index) {
		final Lock readLock = rwLock.readLock();
		readLock.lock();
		try {
			return super.get(index);
		} finally {
			readLock.unlock();
		}
	}

	public long size() {
		final Lock readLock = rwLock.readLock();
		readLock.lock();
		try {
			return super.size();
		} finally {
			readLock.unlock();
		}
	}

}
