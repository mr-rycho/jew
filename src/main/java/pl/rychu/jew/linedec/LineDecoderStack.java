package pl.rychu.jew.linedec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineStack;

public class LineDecoderStack implements LineDecoder {

	private static final Pattern PATTERN_STACK
	 = Pattern.compile("^[ \\t]*at[ \\t]+([^(]+).*$");

	private static final Pattern PATTERN_MORE
	 = Pattern.compile("^[ \\t]*...[ \\t]+[0-9]+[ \\t]+more$");

	// -----------

	@Override
	public LogLine decode(final long filePos, final String line
	 , final int length, final LogLine prevLine) {
		final Matcher matcherStack = PATTERN_STACK.matcher(line);
		String classnameRaw = null;
		boolean matchesAny = false;
		if (matcherStack.matches()) {
			classnameRaw = matcherStack.group(1);
			matchesAny = true;
		} else {
			final Matcher matcherMore = PATTERN_MORE.matcher(line);
			if (matcherMore.matches()) {
				classnameRaw = null;
				matchesAny = true;
			}
		}
		if (matchesAny) {
			final String classname
			 = classnameRaw!=null ? LogElemsCache.getOrPutLogger(classnameRaw) : null;
			final String threadName = prevLine!=null ? prevLine.getThreadName() : null;
			LogLineStack logLineStack = LineDecoderUtil.getLogLineStack(prevLine);
			int stackLineTotal = logLineStack!=null ? logLineStack.getStackLineTotal()+1 : 0;
			int stackLineInCause = logLineStack!=null ? logLineStack.getStackLineInCause()+1 : 0;
			return LogLineStack.create(filePos, length, threadName
			 , classname, stackLineTotal, stackLineInCause);
		} else {
			return null;
		}
	}

}
