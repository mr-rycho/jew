package pl.rychu.jew.linedec;

import pl.rychu.jew.conf.LoggerType;
import pl.rychu.jew.logline.LogLine;

/**
 * Created on 14.07.2017.
 */
public class LineDecoderFull implements LineDecoder {
	private final LineDecoder std;
	private final LineDecoder chain;

	public LineDecoderFull(LoggerType loggerType) {
		LineDecoder empty = new LineDecoderEmpty();
		LineDecoder stack = new LineDecoderStack();
		LineDecoder stackCause = new LineDecoderStackCause();
		LineDecoder term = new LineDecoderText();

		this.std = getStdLineDecoder(loggerType);
		this.chain = new LineDecodersChain(empty, stack, stackCause, std, term);
	}

	@Override
	public LogLine decode(long filePos, String line, int length, LogLine prevLine) {
		return chain.decode(filePos, line, length, prevLine);
	}

	private static LineDecoder getStdLineDecoder(LoggerType loggerType) {
		switch (loggerType) {
			case WILDFLY_STD:
				return new LineDecoderWildStd();
			case GLASSFISH_STD:
				return new LineDecoderGfStd();
			case APP:
				return new LineDecoderApp();
			default:
				throw new IllegalArgumentException("unknown server type: " + loggerType);
		}
	}

}
