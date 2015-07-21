package pl.rychu.jew.logaccess;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import pl.rychu.jew.logline.LogLineFull;

public class LogAccessCache {

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Cache cache = new Cache(50_000);

	private int version = -1;

	// ------------

	public LogLineFull read(long pos, int version) {
		Lock readLock = lock.readLock();
		readLock.lock();
		try {
			if (version != this.version) {
				return null;
			}
			return cache.get(pos);
		} finally {
			readLock.unlock();
		}
	}

	public void write(long pos, LogLineFull logLineFull, int version) {
		Lock writeLock = lock.writeLock();
		writeLock.lock();
		try {
			if (version < this.version) {
				return;
			} else {
				if (version > this.version) {
					this.version = version;
					cache.clear();
				}
				cache.put(pos, logLineFull);
			}
		} finally {
			writeLock.unlock();
		}
	}

	// ====================

	private static class Cache extends LinkedHashMap<Long, LogLineFull> {

		private static final long serialVersionUID = -1990554698615114078L;

		private final int maxSize;

		private Cache(int maxSize) {
			super(16, 0.75f, true);
			this.maxSize = maxSize;
		}

		@Override
		protected boolean removeEldestEntry(Entry<Long, LogLineFull> eldest) {
			return size() > maxSize;
		}
	}

}
