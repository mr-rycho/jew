package pl.rychu.jew.filter;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;



public class LogLineFilterPos implements LogLineFilter {

	private final long minPos;
	private final long maxPos;
	private final String asString;

	// -----

	public LogLineFilterPos(final long minPos, final long minLine
	 , final long maxPos, final long maxLine) {
		super();
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.asString = asString(minLine, maxLine);
	}

	// -----

	@Override
	public boolean needsFullLine() {
		return false;
	}

	@Override
	public boolean apply(LogLine logLine) {
		long filePos = logLine.getFilePos();
		return filePos>=minPos && filePos<=maxPos;
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

	private static String asString(final long minLine, final long maxLine) {
		if (minLine>0L && maxLine<Long.MAX_VALUE) {
			return "line in ["+(minLine+1)+";"+(maxLine+1)+"]";
		} else if (minLine > 0) {
			return "line>="+(minLine+1);
		} else {
			return "line<="+(maxLine+1);
		}
	}

}
