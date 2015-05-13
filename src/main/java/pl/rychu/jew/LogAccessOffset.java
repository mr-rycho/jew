package pl.rychu.jew;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.gl.BadVersionException;



public class LogAccessOffset implements LogAccess {

	private final LogAccess source;

	private final AtomicInteger sourceVersion = new AtomicInteger();

	private Thread readerThread;

	private long offset;
	private int myVersion;
	private long sizeF;
	private long sizeB;

	private final ReadWriteLock locks = new ReentrantReadWriteLock();

	// ----

	private LogAccessOffset(final LogAccess source, final int version
	 , final long offset) {
		this.source = source;
		sourceVersion.set(version);
		final Lock lock = this.locks.writeLock();
		lock.lock();
		try {
			this.offset = offset;
		} finally {
			lock.unlock();
		}
	}

	public static LogAccessOffset create(final LogAccess source, final int version
	 , final long offset) {
		final LogAccessOffset result = new LogAccessOffset(source, version, offset);

		result.readerThread = new Thread(result.new SourceReaderMultiway(offset));
		result.readerThread.start();

		return result;
	}

	@Override
	public void dispose() {
		readerThread.interrupt();
		readerThread = null;
	}

	// ----

	@Override
	public int getVersion() {
		final Lock lock = locks.readLock();
		lock.lock();
		try {
			return myVersion;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long sizeF(int version) throws BadVersionException {
		final Lock lock = locks.readLock();
		lock.lock();
		try {
			return sizeF;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public long sizeB(int version) throws BadVersionException {
		final Lock lock = locks.readLock();
		lock.lock();
		try {
			if (version != this.myVersion) {
				throw new BadVersionException();
			}
			return sizeB;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public LogLine get(long pos, int version) throws BadVersionException {
		long index = 0;
		final Lock lock = locks.readLock();
		lock.lock();
		try {
			if (version != this.myVersion) {
				throw new BadVersionException();
			}
			index = pos - offset;
		} finally {
			lock.unlock();
		}
		return source.get(index, sourceVersion.get());
	}

	@Override
	public LogLineFull getFull(long pos, int version) throws BadVersionException {
		long index = 0;
		final Lock lock = locks.readLock();
		lock.lock();
		try {
			if (version != this.myVersion) {
				throw new BadVersionException();
			}
			index = pos - offset;
		} finally {
			lock.unlock();
		}
		return source.getFull(index, sourceVersion.get());
	}

	// ========================

	private class SourceReaderMultiway implements Runnable {

		private final Logger log = LoggerFactory.getLogger(SourceReaderMultiway.class);

		private long prevMaxIndexF;
		private long prevMinIndexB;

		SourceReaderMultiway(final long offset) {
			prevMaxIndexF = offset;
			prevMinIndexB = offset;
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					process();
				} catch (RuntimeException e) {
					log.error("error during processing", e);
				} catch (BadVersionException e) {
					log.debug("bad version");
					// ignore; version reset will be handled in next cycle
				}

				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					break;
				}
			}
			log.debug("quitting gracefully");
		}

		private void process() throws BadVersionException {
			final int version = source.getVersion();
			if (version != sourceVersion.get()) {
				log.debug("clearing");
				final Lock lock = locks.writeLock();
				lock.lock();
				try {
					offset = 0L;
					sizeF = 0L;
					sizeB = 0;
					myVersion++;
				} finally {
					lock.unlock();
				}
				sourceVersion.set(version);
				prevMaxIndexF = 0L;
				prevMinIndexB = 0L;
			}

			while (true) {
				final long maxIndexF = source.sizeF(version);
				final long minIndexB = -source.sizeB(version);

				if (maxIndexF==prevMaxIndexF && minIndexB==prevMinIndexB) {
					break;
				}

				if (maxIndexF != prevMaxIndexF) {
					final Lock lock = locks.writeLock();
					lock.lock();
					try {
						sizeF = maxIndexF - offset;
					} finally {
						lock.unlock();
					}
					prevMaxIndexF = maxIndexF;
				}

				if (minIndexB != prevMinIndexB) {
					final Lock lock = locks.writeLock();
					lock.lock();
					try {
						sizeB = (-minIndexB) + offset;
					} finally {
						lock.unlock();
					}
					prevMinIndexB = minIndexB;
				}
			}
		}

	}

}
