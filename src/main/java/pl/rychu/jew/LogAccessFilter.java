package pl.rychu.jew;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.filter.LogLineFilter;
import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.gl.GrowingList;
import pl.rychu.jew.gl.GrowingListLocked;



public class LogAccessFilter implements LogAccess {

	private static final Logger log = LoggerFactory.getLogger(LogAccessFilter.class);

	private final LogAccess source;

	private AtomicInteger sourceVersion = new AtomicInteger();

	private final LogLineFilter filter;

	private final GrowingList<Integer> indexF = GrowingListLocked.create(1024);

	private final GrowingList<Integer> indexB = GrowingListLocked.create(1024);

	private Thread readerThread;

	// --------------

	private LogAccessFilter(final LogAccess source, final LogLineFilter filter) {
		this.source = source;
		this.filter = filter;
	}

	public static LogAccess create(final LogAccess source, final LogLineFilter filter
	 , final long startLine) {
		final LogAccessFilter result = new LogAccessFilter(source, filter);

		result.readerThread = new Thread(result.new SourceReaderMultiway(startLine));
		result.readerThread.start();

		return result;
	}

	public void dispose() {
		readerThread.interrupt();
		readerThread = null;
	}

	// -------------

	@Override
	public int getVersion() {
		return indexF.getVersion();
	}

	@Override
	public long sizeF(final int version) throws BadVersionException {
		return indexF.size(version);
	}

	@Override
	public long sizeB(final int version) throws BadVersionException {
		return indexB.size(version);
	}

	@Override
	public LogLine get(final long pos, final int version) throws BadVersionException {
		return source.get(getSourceIndex(pos, version), sourceVersion.get());
	}

	@Override
	public LogLineFull getFull(final long pos, final int version) throws BadVersionException {
		return source.getFull(getSourceIndex(pos, version), sourceVersion.get());
	}

	private int getSourceIndex(final long pos, final int version) throws BadVersionException {
		return pos>=0 ? indexF.get(pos, version) : indexB.get((-pos)-1, version);
	}

	// ========================

	private class SourceReaderMultiway implements Runnable {

		private long prevMaxIndexF;
		private long prevMinIndexB;

		SourceReaderMultiway(final long startLine) {
			prevMaxIndexF = startLine;
			prevMinIndexB = startLine;
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
				indexB.clear();
				indexF.clear();
				sourceVersion.set(version);
				prevMaxIndexF = 0L;
				prevMinIndexB = 0L;
			}

			final int maxSlice = 100;
			while (true) {
				final long maxIndexF = source.sizeF(version);
				final long minIndexB = -source.sizeB(version);

				if (maxIndexF==prevMaxIndexF && minIndexB==prevMinIndexB) {
					break;
				}

				if (maxIndexF != prevMaxIndexF) {
					final long maxF = Math.min(maxIndexF, prevMaxIndexF+maxSlice);
					for (long i=prevMaxIndexF; i<maxF; i++) {
						final boolean applies
						 = filter.needsFullLine()
						 ? filter.apply(source.getFull(i, version))
						 : filter.apply(source.get(i, version));
						if (applies) {
							indexF.add((int)i);
						}
					}
					prevMaxIndexF = maxF;
				}

				if (minIndexB != prevMinIndexB) {
					final long minB = Math.max(minIndexB, prevMinIndexB-maxSlice);
					for (long i=prevMinIndexB-1; i>=minB; i--) {
						final boolean applies
						 = filter.needsFullLine()
						 ? filter.apply(source.getFull(i, version))
						 : filter.apply(source.get(i, version));
						if (applies) {
							indexB.add((int)i);
						}
					}
					prevMinIndexB = minB;
				}
			}
		}

	}


}
