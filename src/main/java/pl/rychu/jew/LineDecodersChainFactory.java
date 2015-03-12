package pl.rychu.jew;

import pl.rychu.jew.linedec.LineDecoder;
import pl.rychu.jew.linedec.LineDecoderEmpty;
import pl.rychu.jew.linedec.LineDecoderStack;
import pl.rychu.jew.linedec.LineDecoderStackCause;
import pl.rychu.jew.linedec.LineDecoderStd;
import pl.rychu.jew.linedec.LineDecoderText;
import pl.rychu.jew.linedec.LineDecodersChain;



public class LineDecodersChainFactory {

	public static LineDecoder getLineDecodersChain() {
		final LineDecoder empty = new LineDecoderEmpty();
		final LineDecoder stack = new LineDecoderStack();
		final LineDecoder stackCause = new LineDecoderStackCause();
		final LineDecoder std = new LineDecoderStd();
		final LineDecoder term = new LineDecoderText();

		return new LineDecodersChain(empty, stack, stackCause, std, term);
	}

}
