package pl.rychu.jew.linedec;

import pl.rychu.jew.LogLine;

public class LineDecoderText implements LineDecoder {

	@Override
	public LogLine decode(long filePos, String line) {
		return LogLine.createText(filePos);
	}

}
