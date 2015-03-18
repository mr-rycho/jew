package pl.rychu.jew.linedec;

import pl.rychu.jew.LogLine;

public class LineDecoderEmpty implements LineDecoder {

	@Override
	public LogLine decode(final long filePos, final String line, final int length) {
		if (line.isEmpty()) {
			return LogLine.createText(filePos, length);
		} else {
			return null;
		}
	}

}
