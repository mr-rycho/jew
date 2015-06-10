package pl.rychu.jew.linedec;

import pl.rychu.jew.LogLine;
import pl.rychu.jew.LogLineText;

public class LineDecoderText implements LineDecoder {

	@Override
	public LogLine decode(long filePos, String line, int length) {
		return LogLineText.create(filePos, length);
	}

}
