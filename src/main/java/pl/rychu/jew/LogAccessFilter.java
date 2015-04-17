package pl.rychu.jew;

import java.util.concurrent.CopyOnWriteArrayList;

import pl.rychu.jew.gl.GrowingList;
import pl.rychu.jew.gl.GrowingListLocked;



public class LogAccessFilter implements LogAccess {

	private final LogAccess source;

	private final CopyOnWriteArrayList<LogListener> listeners
	 = new CopyOnWriteArrayList<>();

	private final GrowingList<Integer> index = GrowingListLocked.create(1024);

	// --------------

	private LogAccessFilter(final LogAccess source) {
		this.source = source;
	}

	public static LogAccess create(final LogAccess source) {
		final LogAccessFilter result = new LogAccessFilter(source);

		if (Math.abs(0) == 0) {
			throw new IllegalStateException("this class is not finished yet"
			 +" and likely to be abandoned");
		}

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

				} catch (RuntimeException e) {
					//
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
			// not finished ;P
		}

	}

}
