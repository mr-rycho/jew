package pl.rychu.jew.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;



public class LogLineThreadFilter implements LogLineFilter {

	private final Set<String> threadNames;

	private final String asString;

	// -----------

	public LogLineThreadFilter(String... threadNames) {
		List<String> list = Arrays.asList(threadNames);
		this.threadNames = new HashSet<>(list);
		this.asString = asString(list);
	}

	// ----------------

	@Override
	public boolean needsFullLine() {
		return false;
	}

	@Override
	public boolean apply(final LogLine logLine) {
		return threadNames.contains(logLine.getThreadName());
	}

	@Override
	public boolean apply(LogLineFull logLineFull) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return asString;
	}

	private String asString(Collection<String> list) {
		if (list.size() == 1) {
			String any = list.iterator().next();
			return "thread=\""+any+"\"";
		} else {
			return "->";
		}
	}

}
