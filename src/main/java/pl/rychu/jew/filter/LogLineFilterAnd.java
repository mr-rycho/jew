package pl.rychu.jew.filter;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;

public class LogLineFilterAnd implements LogLineFilter {

	private final LogLineFilter filterOne;
	private final LogLineFilter filterTwo;
	private final boolean needsFullLine;
	private final String toString;

	// -------------

	public LogLineFilterAnd(final LogLineFilter filterOne, final LogLineFilter filterTwo) {
		this.needsFullLine = filterOne.needsFullLine() || filterTwo.needsFullLine();
		this.filterOne = filterOne;
		this.filterTwo = filterTwo;
		this.toString = asString(filterOne, filterTwo);
	}

	// -------------

	@Override
	public boolean needsFullLine() {
		return needsFullLine;
	}

	@Override
	public boolean apply(LogLine logLine) {
		if (needsFullLine) {
			throw new IllegalStateException("full line is required");
		}
		return filterOne.apply(logLine) && filterTwo.apply(logLine);
	}

	@Override
	public boolean apply(LogLineFull logLineFull) {
		if (!needsFullLine) {
			throw new IllegalStateException("full line is required");
		}
		return applyToFilter(filterOne, logLineFull) && applyToFilter(filterTwo, logLineFull);
	}

	private boolean applyToFilter(LogLineFilter filter, LogLineFull logLineFull) {
		return filter.needsFullLine() ? filter.apply(logLineFull)
		 : filter.apply(logLineFull.getLogLine());
	}

	@Override
	public String toString() {
		return toString;
	}

	private String asString(final LogLineFilter filterOne, final LogLineFilter filterTwo) {
		return "("+filterOne+") AND ("+filterTwo+")";
	}

}
