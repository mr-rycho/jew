package pl.rychu.jew.filter;

import pl.rychu.jew.LogLine;
import pl.rychu.jew.LogLineFull;



public class LogLineThreadFilter implements LogLineFilter {

	private final String threadName;

	public LogLineThreadFilter(final String threadName) {
		this.threadName = threadName;
	}

	// ----------------

	@Override
	public boolean needsFullLine() {
		return false;
	}

	@Override
	public boolean apply(final LogLine logLine) {
		return threadName.equals(logLine.getThreadName());
	}

	@Override
	public boolean apply(LogLineFull logLineFull) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "thread=\""+threadName+"\"";
	}

}
