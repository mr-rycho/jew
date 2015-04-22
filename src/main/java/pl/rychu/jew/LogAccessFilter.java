package pl.rychu.jew;

import java.util.concurrent.atomic.AtomicInteger;

import pl.rychu.jew.filter.LogLineFilter;
import pl.rychu.jew.gl.GrowingList;
import pl.rychu.jew.gl.GrowingListLocked;



public class LogAccessFilter implements LogAccess {

	private final LogAccess source;

	private AtomicInteger sourceVersion = new AtomicInteger();

	private final LogLineFilter filter;

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
	public int getVersion() {
		return index.getVersion();
	}

	@Override
	public long size(final int version) {
		return index.size(version);
	}

	@Override
	public LogLine get(final long pos, final int version) {
		return source.get(index.get(pos, version), sourceVersion.get());
	}

	@Override
	public LogLineFull getFull(final long pos, final int version) {
		return source.getFull(index.get(pos, version), sourceVersion.get());
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
					index.clear();
					sourceVersion.set(source.getVersion());
				}

				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private void process() {
			final int version = sourceVersion.get();
			final long size = source.size(version);
			if (size != prevSize) {
				for (long i=prevSize; i<size; i++) {
					final boolean applies
					 = filter.needsFullLine()
					 ? filter.apply(source.getFull(i, version))
					 : filter.apply(source.get(i, version));
					if (applies) {
						index.add((int)i);
					}
				}
				prevSize = size;
			}
		}

	}

}
