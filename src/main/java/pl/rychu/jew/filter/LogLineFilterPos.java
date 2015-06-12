package pl.rychu.jew.filter;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;



public class LogLineFilterPos implements LogLineFilter {

	private final long minPos;
	private final String asString;

	// -----

	public LogLineFilterPos(final long minPos, final long minLine) {
		super();
		this.minPos = minPos;
		this.asString = asString(minLine);
	}

	// -----

	@Override
	public boolean needsFullLine() {
		return false;
	}

	@Override
	public boolean apply(LogLine logLine) {
		return logLine.getFilePos() >= minPos;
	}

	@Override
	public boolean apply(LogLineFull logLineFull) {
		throw new UnsupportedOperationException();
	}

	// -----

	@Override
	public String toString() {
		return asString;
	}

	private static String asString(final long minLine) {
		return "line>="+(minLine+1);
	}

}
