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

	private class SourceReaderForward implements Runnable {

		private long prevSizeF;

		SourceReaderForward(final long startLine) {
			prevSizeF = startLine;
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

		}

		private void process() throws BadVersionException {
			final int version = source.getVersion();
			if (version != sourceVersion.get()) {
				log.debug("clearing by F");
				indexF.clear();
				indexB.clear();
				sourceVersion.set(version);
				prevSizeF = 0L;
			}
			final long sizeF = source.sizeF(version);
			if (sizeF != prevSizeF) {
				for (long i=prevSizeF; i<sizeF; i++) {
					final boolean applies
					 = filter.needsFullLine()
					 ? filter.apply(source.getFull(i, version))
					 : filter.apply(source.get(i, version));
					if (applies) {
						indexF.add((int)i);
					}
				}
				prevSizeF = sizeF;
			}
		}

	}

	// ---------

	private class SourceReaderBackward implements Runnable {

		private long prevSizeB;

		SourceReaderBackward(final long startLine) {
			log.debug("start line: {}", startLine);
			prevSizeB = startLine;
		}

		@Override
		public void run() {
			log.debug("running");
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

			log.debug("quit");
		}

		private void process() throws BadVersionException {
			final int version = source.getVersion();
			if (version != sourceVersion.get()) {
				log.debug("clearing by B");
				indexF.clear();
				indexB.clear();
				sourceVersion.set(version);
				prevSizeB = 0L;
			}
			final long size = -source.sizeB(version);
			if (size != prevSizeB) {
				for (long i=prevSizeB-1; i>=size; i--) {
					final boolean applies
					 = filter.needsFullLine()
					 ? filter.apply(source.getFull(i, version))
					 : filter.apply(source.get(i, version));
					if (applies) {
						indexB.add((int)i);
					}
				}
				prevSizeB = size;
			}
		}

	}

}
