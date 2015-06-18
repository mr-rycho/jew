package pl.rychu.jew.linedec;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineText;

public class LineDecoderText implements LineDecoder {

	@Override
	public LogLine decode(long filePos, String line, int length, LogLine prevLine) {
		String threadName = prevLine!=null ? prevLine.getThreadName() : "";
		threadName = threadName==null ? "" : threadName;
		return LogLineText.create(filePos, length, threadName);
	}

}
