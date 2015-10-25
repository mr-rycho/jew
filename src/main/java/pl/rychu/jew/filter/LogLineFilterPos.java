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
		StringBuilder sb = new StringBuilder(100);
		sb.append("line in [");
		sb.append(minLine + 1);
		sb.append(";");
		if (maxLine < Long.MAX_VALUE) {
			sb.append(maxLine + 1);
			sb.append("]");
		} else {
			sb.append((char)0x221e);
			sb.append(")");
		}
		return sb.toString();
	}

}
