package pl.rychu.jew.filter;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;

public class LogLineFilterAll implements LogLineFilter {

	@Override
	public boolean needsFullLine() {
		return false;
	}

	@Override
	public boolean apply(LogLine logLine) {
		return true;
	}

	@Override
	public boolean apply(LogLineFull logLineFull) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "no filter";
	}

}
