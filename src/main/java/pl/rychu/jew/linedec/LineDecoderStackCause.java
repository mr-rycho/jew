package pl.rychu.jew.linedec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineStackCause;

public class LineDecoderStackCause implements LineDecoder {

	private static final Pattern PATTERN
	 = Pattern.compile("^Caused by:[ \\t]*([^:]+):.*$");

	// --------

	@Override
	public LogLine decode(final long filePos, final String line
	 , final int length, final LogLine prevLine) {
		final Matcher matcherStack = PATTERN.matcher(line);
		if (matcherStack.matches()) {
			final String classname = matcherStack.group(1);
			final String threadName = prevLine!=null ? prevLine.getThreadName() : null;
			return LogLineStackCause.create(filePos, length, threadName
			 , LogElemsCache.getOrPutLogger(classname));
		} else {
			return null;
		}
	}

}
