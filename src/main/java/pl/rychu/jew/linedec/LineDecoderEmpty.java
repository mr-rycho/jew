package pl.rychu.jew.linedec;

import pl.rychu.jew.LogLine;
import pl.rychu.jew.LogLineText;

public class LineDecoderEmpty implements LineDecoder {

	@Override
	public LogLine decode(final long filePos, final String line, final int length) {
		if (line.isEmpty()) {
			return LogLineText.create(filePos, length);
		} else {
			return null;
		}
	}

}
