package pl.rychu.jew.filter;

import pl.rychu.jew.LogLine;
import pl.rychu.jew.LogLineFull;

public interface LogLineFilter {

	boolean needsFullLine();

	boolean apply(LogLine logLine);

	boolean apply(LogLineFull logLineFull);

}
