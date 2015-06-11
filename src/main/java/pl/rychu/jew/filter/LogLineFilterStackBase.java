package pl.rychu.jew.filter;

import pl.rychu.jew.linedec.LineDecoderUtil;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;
import pl.rychu.jew.logline.LogLineStack;

public abstract class LogLineFilterStackBase implements LogLineFilter {

	private final int maxStackLinesTotal;
	private final int maxStackLinesInCause;
	private final String toString;

	// -----------

	protected LogLineFilterStackBase(final int maxStackLinesTotal
	 , final int maxStackLinesInCause) {
		this.maxStackLinesTotal = maxStackLinesTotal;
		this.maxStackLinesInCause = maxStackLinesInCause;
		this.toString = asString(maxStackLinesTotal, maxStackLinesInCause);
	}

	// -----------

	@Override
	public boolean needsFullLine() {
		return false;
	}

	@Override
	public boolean apply(LogLine logLine) {
		LogLineStack logLineStack = LineDecoderUtil.getLogLineStack(logLine);
		if (logLineStack != null) {
			return logLineStack.getStackLineTotal() < maxStackLinesTotal
			 && logLineStack.getStackLineInCause() < maxStackLinesInCause;
		} else {
			return true;
		}
	}

	@Override
	public boolean apply(LogLineFull logLineFull) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return toString;
	}

	private static String asString(final int maxStackLinesTotal, final int maxStackLinesInCause) {
		return "stack="+(maxStackLinesTotal==Integer.MAX_VALUE ? "-" : maxStackLinesTotal)
		 +"/"+(maxStackLinesInCause==Integer.MAX_VALUE ? "-" : maxStackLinesInCause);
	}

}
