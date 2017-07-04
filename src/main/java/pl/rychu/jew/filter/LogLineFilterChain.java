package pl.rychu.jew.filter;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 04.07.2017.
 */
public class LogLineFilterChain {
	private final List<LogLineFilter> filtersForIndex;
	private final List<LogLineFilter> filtersForFullLines;
	private final String asString;

	public LogLineFilterChain() {
		this(null);
	}

	public LogLineFilterChain(List<LogLineFilter> filters) {
		this.filtersForIndex = new ArrayList<>();
		this.filtersForFullLines = new ArrayList<>();

		if (filters!=null && !filters.isEmpty()) {
			for (LogLineFilter filter: filters) {
				(filter.needsFullLine() ? filtersForFullLines : filtersForIndex).add(filter);
			}
			if (filters.size() == 1) {
				asString = filters.get(0).toString();
			} else {
				asString = filters.stream().map(Object::toString)
				 .collect(Collectors.joining(" AND ", "(", ")"));
			}
		} else {
			asString = "no filter";
		}
	}

	public boolean apply(LogLine logLine) {
		for (LogLineFilter filter: filtersForIndex) {
			if (!filter.apply(logLine)) {
				return false;
			}
		}
		return true;
	}

	public boolean needsFullLine() {
		return !filtersForFullLines.isEmpty();
	}

	public boolean apply(LogLineFull logLineFull) {
		for (LogLineFilter filter: filtersForFullLines) {
			if (!filter.apply(logLineFull)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return asString;
	}
}
