package pl.rychu.jew.linedec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineStd;



public class LineDecoderGfStd implements LineDecoder {

	private static final Pattern PATTERN_STD
	 = Pattern.compile(
	  "^"+"\\["+"([-+:., 0-9T]+)"+"\\]" // time
	  +"[ \\t]+"+"\\["+"[^]]*"+"\\]" // glassfish 4.1
	  +"[ \\t]+"+"\\["+"([^]]*)"+"\\]" // level
	  +"[ \\t]+"+"\\["+"[^]]*"+"\\]" // sth
	  +"[ \\t]+"+"\\["+"([^]]*)"+"\\]" // class
	  +"[ \\t]+"+"\\["+"[^]]*ThreadName=([^]]+)"+"\\]" // thread
	  +"[ \\t]+"+".*$"
	 );

	// -----------------

	@Override
	public LogLine decode(final long filePos, final String line
	 , final int length, final LogLine prevLine) {
		final Matcher matcherStd = PATTERN_STD.matcher(line);
		if (matcherStd.matches()) {
			final String levelRaw = matcherStd.group(2);
			final String classnameRaw = matcherStd.group(3);
			final String threadRaw = matcherStd.group(4);

			final String level = LogElemsCache.getOrPutLevel(levelRaw);
			final String threadName = LogElemsCache.getOrPutThread(threadRaw);
			final String classname = LogElemsCache.getOrPutLogger(classnameRaw);
			return LogLineStd.create(filePos, length, 0L, level, classname, threadName);
		} else {
			return null;
		}
	}

}
