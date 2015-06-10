package pl.rychu.jew.linedec;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineText;

public class LineDecoderEmpty implements LineDecoder {

	@Override
	public LogLine decode(final long filePos, final String line
	 , final int length, final LogLine prevLine) {
		if (line.isEmpty()) {
			return LogLineText.create(filePos, length);
		} else {
			return null;
		}
	}

}
