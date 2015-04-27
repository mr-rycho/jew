package pl.rychu.jew;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.filter.LogLineFilter;
import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.gl.DoubleList;



public class LogAccessFilter implements LogAccess {

	private static final Logger log = LoggerFactory.getLogger(LogAccessFilter.class);

	private final LogAccess source;

	private AtomicInteger sourceVersion = new AtomicInteger();

	private final LogLineFilter filter;

	private final DoubleList<Integer> index = DoubleList.create(1024);

	private Thread threadForward;
	private Thread threadBackward;

	// --------------

	private LogAccessFilter(final LogAccess source, final LogLineFilter filter) {
		this.source = source;
		this.filter = filter;
	}

	public static LogAccess create(final LogAccess source, final LogLineFilter filter
	 , final long startLine) {
		final LogAccessFilter result = new LogAccessFilter(source, filter);

		result.threadForward = new Thread(result.new SourceReaderForward(startLine));
		result.threadForward.start();
		result.threadBackward = new Thread(result.new SourceReaderBackward(startLine));
		result.threadBackward.start();

		return result;
	}

	public void dispose() {
		threadForward.interrupt();
		threadForward = null;
		threadBackward.interrupt();
		threadBackward = null;
	}

	// -------------

	@Override
	public int getVersion() {
		return index.getVersion();
	}

	@Override
	public long size(final int version) throws BadVersionException {
		return index.size(version);
	}

	@Override
	public LogLine get(final long pos, final int version) throws BadVersionException {
		return source.get(index.get(pos, version), sourceVersion.get());
	}

	@Override
	public LogLineFull getFull(final long pos, final int version) throws BadVersionException {
		return source.getFull(index.get(pos, version), sourceVersion.get());
	}

	// ========================

	private class SourceReaderForward implements Runnable {

		private long prevSize;

		SourceReaderForward(final long startLine) {
			prevSize = startLine;
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					process();
				} catch (RuntimeException e) {
					log.error("error during processing", e);
				} catch (BadVersionException e) {
					// ignore; version reset will be handled in next cycle
				}

				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private void process() throws BadVersionException {
			final int version = source.getVersion();
			if (version != sourceVersion.get()) {
				index.clear();
				sourceVersion.set(version);
				prevSize = 0;
			}
			final long size = source.size(version);
			if (size != prevSize) {
				for (long i=prevSize; i<size && !Thread.interrupted(); i++) {
					final boolean applies
					 = filter.needsFullLine()
					 ? filter.apply(source.getFull(i, version))
					 : filter.apply(source.get(i, version));
					if (applies) {
						index.addForward((int)i);
					}
				}
				prevSize = size;
			}
		}

	}

	// ---------

	private class SourceReaderBackward implements Runnable {

		private long startSize;

		SourceReaderBackward(final long startLine) {
			startSize = startLine;
		}

		@Override
		public void run() {
			try {
				process();
			} catch (RuntimeException e) {
				log.error("error during processing", e);
			} catch (BadVersionException e) {
				// ignore; version reset will be handled in next cycle
			}
		}

		private void process() throws BadVersionException {
			final int version = source.getVersion();
			if (version != sourceVersion.get()) {
				return;
			}
			for (long i=startSize-1; i>=0; i--) {
				final boolean applies
				 = filter.needsFullLine()
				 ? filter.apply(source.getFull(i, version))
				 : filter.apply(source.get(i, version));
				if (applies) {
					index.addBackward((int)i);
				}
			}
		}

	}

}
