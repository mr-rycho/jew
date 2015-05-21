package pl.rychu.jew;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.filter.LogLineFilter;
import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.gl.DoubleListVer;
import pl.rychu.jew.gl.DoubleListVerLocked;
import pl.rychu.jew.gl.DoubleListVerSimple;



@Deprecated
public class LogAccessFilter {

	private static final Logger log = LoggerFactory.getLogger(LogAccessFilter.class);

	private final LogAccess source;

	private final AtomicInteger sourceVersion = new AtomicInteger();

	private final LogLineFilter filter;

	private final DoubleListVer<Integer> index = DoubleListVerLocked.create(DoubleListVerSimple.create(1024));

	private Thread readerThread;

	// --------------

	private LogAccessFilter(final LogAccess source, final int version
	 , final LogLineFilter filter) {
		this.source = source;
		sourceVersion.set(version);
		this.filter = filter;
	}

	public static LogAccessFilter create(final LogAccess source, final int version
	 , final LogLineFilter filter, final long startLine) {
		final LogAccessFilter result = new LogAccessFilter(source, version, filter);

		result.readerThread = new Thread(result.new SourceReaderMultiway(startLine));
		result.readerThread.start();

		return result;
	}

	public void dispose() {
		readerThread.interrupt();
		readerThread = null;
	}

	// -------------

	public int getVersion() {
		return index.getVersion();
	}

	public long sizeF(final int version) throws BadVersionException {
		return index.sizeF(version);
	}

	public long sizeB(final int version) throws BadVersionException {
		return index.sizeB(version);
	}

	public LogLine get(final long pos, final int version) throws BadVersionException {
		return source.get(index.get(pos, version), sourceVersion.get());
	}

	public LogLineFull getFull(final long pos, final int version) throws BadVersionException {
		return source.getFull(index.get(pos, version), sourceVersion.get());
	}

	public long getRootIndex(final long pos, final int version)
	 throws BadVersionException {
		return index.get(pos, version);
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
				index.clear();
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
							index.addForward((int)i);
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
							index.addBackward((int)i);
						}
					}
					prevMinIndexB = minB;
				}
			}
		}

	}


}
