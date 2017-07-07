package pl.rychu.jew.filter;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 07.07.2017.
 */
public class LogLineFilterRegex implements LogLineFilter {
	private final Pattern pattern;
	private final String asString;

	public LogLineFilterRegex(Pattern pattern) {
		this.pattern = pattern;
		this.asString = "~ '"+pattern+"'";
	}

	@Override
	public boolean needsFullLine() {
		return true;
	}

	@Override
	public boolean apply(LogLine logLine) {
		throw new IllegalStateException("call apply(LogLineFull)");
	}

	@Override
	public boolean apply(LogLineFull logLineFull) {
		String fullTextRaw = logLineFull.getFullText();
		String fullText = fullTextRaw != null ? fullTextRaw : "";
		Matcher matcher = pattern.matcher(fullText);
		return matcher.find();
	}

	@Override
	public String toString() {
		return asString;
	}

}
