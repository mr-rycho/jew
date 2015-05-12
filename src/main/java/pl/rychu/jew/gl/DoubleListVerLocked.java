package pl.rychu.jew.gl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DoubleListVerLocked<T> implements DoubleListVer<T> {

	private final DoubleListVer<T> delegate;

	private final ReadWriteLock locks = new ReentrantReadWriteLock();

	// -----------

	protected DoubleListVerLocked(final DoubleListVer<T> delegate) {
		this.delegate = delegate;
	}

	public static <T> DoubleListVerLocked<T> create(final DoubleListVer<T> delegate) {
		final DoubleListVerLocked<T> result = new DoubleListVerLocked<>(delegate);

		return result;
	}

	// ----------

	@Override
	public void addForward(T value) {
		final Lock lock = locks.writeLock();
		lock.lock();
		try {
			delegate.addForward(value);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addBackward(T value) {
		final Lock lock = locks.writeLock();
		lock.lock();
		try {
			delegate.addBackward(value);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void clear() {
		final Lock lock = locks.writeLock();
		lock.lock();
		try {
			delegate.clear();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public int getVersion() {
		final Lock lock = locks.readLock();
		lock.lock();
		try {
			return delegate.getVersion();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long sizeB(final int version) throws BadVersionException {
		final Lock lock = locks.readLock();
		lock.lock();
		try {
			return delegate.sizeB(version);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long sizeF(final int version) throws BadVersionException {
		final Lock lock = locks.readLock();
		lock.lock();
		try {
			return delegate.sizeF(version);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public T get(final long index, final int version) throws BadVersionException {
		final Lock lock = locks.readLock();
		lock.lock();
		try {
			return delegate.get(index, version);
		} finally {
			lock.unlock();
		}
	}

}
