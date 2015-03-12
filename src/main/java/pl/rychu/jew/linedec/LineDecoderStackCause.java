package pl.rychu.jew.linedec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.rychu.jew.LogLine;

public class LineDecoderStackCause implements LineDecoder {

	private static final Pattern PATTERN
	 = Pattern.compile("^Caused by:[ \\t]*([^:]+):.*$");

	// --------

	@Override
	public LogLine decode(long filePos, String line) {
		final Matcher matcherStack = PATTERN.matcher(line);
		if (matcherStack.matches()) {
			final String classname = matcherStack.group(1);
			return LogLine.createStackCause(filePos, LogElemsCache.getOrPutLogger(classname));
		} else {
			return null;
		}
	}

}
