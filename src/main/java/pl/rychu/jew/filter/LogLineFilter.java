package pl.rychu.jew.filter;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;

public interface LogLineFilter {

	boolean needsFullLine();

	boolean apply(LogLine logLine);

	boolean apply(LogLineFull logLineFull);

}
