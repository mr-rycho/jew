package pl.rychu.jew.linedec;

import pl.rychu.jew.LogLine;

public class LineDecoderText implements LineDecoder {

	@Override
	public LogLine decode(long filePos, String line, int length) {
		return LogLine.createText(filePos, length);
	}

}
