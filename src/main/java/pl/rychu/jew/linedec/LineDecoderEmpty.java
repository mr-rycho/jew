package pl.rychu.jew.linedec;

import pl.rychu.jew.LogLine;

public class LineDecoderEmpty implements LineDecoder {

	@Override
	public LogLine decode(final long filePos, final String line) {
		if (line.isEmpty()) {
			return LogLine.createText(filePos);
		} else {
			return null;
		}
	}

}
