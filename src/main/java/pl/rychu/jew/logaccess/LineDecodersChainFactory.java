package pl.rychu.jew.logaccess;

import pl.rychu.jew.conf.LoggerType;
import pl.rychu.jew.linedec.LineDecoder;
import pl.rychu.jew.linedec.LineDecoderApp;
import pl.rychu.jew.linedec.LineDecoderEmpty;
import pl.rychu.jew.linedec.LineDecoderGfStd;
import pl.rychu.jew.linedec.LineDecoderStack;
import pl.rychu.jew.linedec.LineDecoderStackCause;
import pl.rychu.jew.linedec.LineDecoderWildStd;
import pl.rychu.jew.linedec.LineDecoderText;
import pl.rychu.jew.linedec.LineDecodersChain;



public class LineDecodersChainFactory {

	public static LineDecoder getLineDecodersChain(LoggerType loggerType) {
		final LineDecoder empty = new LineDecoderEmpty();
		final LineDecoder stack = new LineDecoderStack();
		final LineDecoder stackCause = new LineDecoderStackCause();
		final LineDecoder std = getStdLineDecoder(loggerType);
		final LineDecoder term = new LineDecoderText();

		return new LineDecodersChain(empty, stack, stackCause, std, term);
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
			throw new IllegalArgumentException("unknown server type: "+loggerType);
		}
	}

}
