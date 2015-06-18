package pl.rychu.jew.linedec;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineText;

public class LineDecoderEmpty implements LineDecoder {

	@Override
	public LogLine decode(final long filePos, final String line
	 , final int length, final LogLine prevLine) {
		if (line.isEmpty()) {
			String threadName = prevLine!=null ? prevLine.getThreadName() : "";
			threadName = threadName==null ? "" : threadName;
			return LogLineText.create(filePos, length, threadName);
		} else {
			return null;
		}
	}

}
