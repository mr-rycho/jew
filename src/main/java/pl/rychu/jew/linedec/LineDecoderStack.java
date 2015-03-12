package pl.rychu.jew.linedec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.rychu.jew.LogLine;

public class LineDecoderStack implements LineDecoder {

	private static final Pattern PATTERN_STACK
	 = Pattern.compile("^[ \\t]*at[ \\t]+([^(]+).*$");

	// -----------

	@Override
	public LogLine decode(final long filePos, final String line) {
		final Matcher matcherStack = PATTERN_STACK.matcher(line);
		if (matcherStack.matches()) {
			final String classname = matcherStack.group(1);
			return LogLine.createStack(filePos, LogElemsCache.getOrPutLogger(classname));
		} else {
			return null;
		}
	}

}
