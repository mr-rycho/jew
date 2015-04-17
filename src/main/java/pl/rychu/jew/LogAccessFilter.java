package pl.rychu.jew;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.filter.LogLineFilter;
import pl.rychu.jew.gl.GrowingList;
import pl.rychu.jew.gl.GrowingListLocked;



public class LogAccessFilter implements LogAccess {

	private static final Logger log = LoggerFactory.getLogger(LogAccessFilter.class);

	private final LogAccess source;

	private final LogLineFilter filter;

	private final CopyOnWriteArrayList<LogListener> listeners
	 = new CopyOnWriteArrayList<>();

	private final GrowingList<Integer> index = GrowingListLocked.create(1024);

	// --------------

	private LogAccessFilter(final LogAccess source, final LogLineFilter filter) {
		this.source = source;
		this.filter = filter;
	}

	public static LogAccess create(final LogAccess source, final LogLineFilter filter) {
		final LogAccessFilter result = new LogAccessFilter(source, filter);

		new Thread(result.new SourceReader()).start();

		return result;
	}

	// -------------

	@Override
	public void addLogListener(LogListener l) {
		listeners.add(l);
	}

	@Override
	public void removeLogListener(LogListener l) {
		listeners.remove(l);
	}

	@Override
	public long size() {
		return index.size();
	}

	@Override
	public LogLine get(long pos) {
		return source.get(index.get(pos));
	}

	@Override
	public LogLineFull getFull(long pos) {
		return source.getFull(index.get(pos));
	}

	// ========================

	private class SourceReader implements Runnable {

		private long prevSize = 0;

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					process();
				} catch (RuntimeException e) {
					clearIndexAndNotify();
				}

				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private void process() {
			final long size = source.size();
			if (size != prevSize) {
				if (size < prevSize) {
					clearIndexAndNotify();
				}
				for (long i=prevSize; i<size; i++) {
					final boolean applies
					 = filter.needsFullLine()
					 ? filter.apply(source.getFull(i))
					 : filter.apply(source.get(i));
					if (applies) {
						addToIndexAndNotify(i);
					}
				}
				prevSize = size;
			}
		}

		private void clearIndexAndNotify() {
			index.clear();
			prevSize = 0;
			for (final LogListener li: listeners) {
				try {
					li.fileWasReset();
				} catch (RuntimeException e) {
					log.error("swollowing error", e);
				}
			}
		}

		private void addToIndexAndNotify(final long lineNum) {
			index.add((int)lineNum);
			for (final LogListener li: listeners) {
				try {
					li.linesAdded();
				} catch (RuntimeException e) {
					log.error("swollowing error", e);
				}
			}
		}

	}

}
