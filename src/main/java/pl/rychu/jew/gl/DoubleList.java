package pl.rychu.jew.gl;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



@Deprecated
public class DoubleList<T> {

	private final GrowingListVer<T> forwardList;

	private final GrowingListVer<T> backwardList;

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	// --------

	protected DoubleList(final int arraySize) {
		forwardList = GrowingListVer.create(arraySize);
		backwardList = GrowingListVer.create(arraySize);
	}

	public static <T> DoubleList<T> create(final int arraySize) {
		return new DoubleList<T>(arraySize);
	}

	// --------

	public void addForward(final T value) {
		lock.writeLock().lock();
		try {
			forwardList.add(value);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void addBackward(final T value) {
		lock.writeLock().lock();
		try {
			backwardList.add(value);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void clear() {
		lock.writeLock().lock();
		try {
			forwardList.clear();
			backwardList.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}

	// ----------

	public int getVersion() {
		lock.readLock().lock();
		try {
			return forwardList.getVersion();
		} finally {
			lock.readLock().unlock();
		}
	}

	public T get(final long index, final int version) throws BadVersionException {
		lock.readLock().lock();
		try {
			final long bs = backwardList.size(version);
			if (index < bs) {
				return backwardList.get(bs-1-index, version);
			} else {
				return forwardList.get(index-bs, version);
			}
		} finally {
			lock.readLock().unlock();
		}
	}

	public long size(final int version) throws BadVersionException {
		lock.readLock().lock();
		try {
			return forwardList.size(version)+backwardList.size(version);
		} finally {
			lock.readLock().unlock();
		}
	}

}
