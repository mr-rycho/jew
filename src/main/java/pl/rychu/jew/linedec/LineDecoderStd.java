package pl.rychu.jew.linedec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineStd;



public class LineDecoderStd implements LineDecoder {

	private static final Pattern PATTERN_STD
	 = Pattern.compile(
	  "^([-:, 0-9]+)"
	  +"[ \\t]+"+"([A-Z]+)"
	  +"[ \\t]+"+"\\[([^]]+)\\]"
	  +"[ \\t]+"+"(.*)$"
	 );

	// -----------------

	@Override
	public LogLine decode(final long filePos, final String line
	 , final int length, final LogLine prevLine) {
		final Matcher matcherStd = PATTERN_STD.matcher(line);
		if (matcherStd.matches()) {
			final String levelRaw = matcherStd.group(2);
			final String classnameRaw = matcherStd.group(3);
			final String threadRaw = getThreadName(matcherStd.group(4));

			final String level = LogElemsCache.getOrPutLevel(levelRaw);
			final String classname = LogElemsCache.getOrPutLogger(classnameRaw);
			final String threadName = LogElemsCache.getOrPutThread(threadRaw);
			return LogLineStd.create(filePos, length, 0L, level, classname, threadName);
		} else {
			return null;
		}
	}

	private static String getThreadName(final String remain) {
		if (remain.isEmpty() || !remain.startsWith("(")) {
			return "";
		} else {
			final int len = remain.length();
			int pars = 1;
			for (int i=1; i<len; i++) {
				final char c = remain.charAt(i);
				if (c == ')') {
					pars--;
					if (pars <= 0) {
						return remain.substring(1, i);
					}
				} else if (c == '(') {
					pars++;
				}
			}
			return ""; // unbalanced parentheses
		}
	}

}
