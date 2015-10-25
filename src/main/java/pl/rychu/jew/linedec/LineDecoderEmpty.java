package pl.rychu.jew.linedec;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineText;

public class LineDecoderEmpty implements LineDecoder {

	@Override
	public LogLine decode(final long filePos, final String line
	 , final int length, final LogLine prevLine) {
		if (isBlank(line)) {
			String threadName = prevLine!=null ? prevLine.getThreadName() : "";
			threadName = threadName==null ? "" : threadName;
			return LogLineText.create(filePos, 0, threadName);
		} else {
			return null;
		}
	}

	private static boolean isBlank(String str) {
		if (str==null || str.isEmpty()) {
			return true;
		}
		int len = str.length();
		for (int i=0; i<len; i++) {
			char c = str.charAt(i);
			if (c!=' ' && c!='\t') {
				return false;
			}
		}
		return true;
	}
}
