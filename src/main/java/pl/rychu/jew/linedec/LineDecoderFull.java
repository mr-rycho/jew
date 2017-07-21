package pl.rychu.jew.linedec;

import pl.rychu.jew.logline.LogLine;

/**
 * Created on 14.07.2017.
 */
public class LineDecoderFull implements LineDecoder {
	private final LineDecoderRegex std;
	private final LineDecoder chain;

	public LineDecoderFull(LineDecoderCfg lineDecoderCfg) {
		LineDecoder empty = new LineDecoderEmpty();
		LineDecoder stack = new LineDecoderStack();
		LineDecoder stackCause = new LineDecoderStackCause();
		LineDecoder term = new LineDecoderText();

		this.std = new LineDecoderRegex(lineDecoderCfg);
		this.chain = new LineDecodersChain(empty, stack, stackCause, std, term);
	}

	public void reconfig(LineDecoderCfg lineDecoderCfg) {
		std.reconfig(lineDecoderCfg);
	}

	@Override
	public LogLine decode(long filePos, String line, int length, LogLine prevLine) {
		return chain.decode(filePos, line, length, prevLine);
	}

}
