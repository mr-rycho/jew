package pl.rychu.jew.linedec;

import java.util.Arrays;

import pl.rychu.jew.LogLine;

public class LineDecodersChain implements LineDecoder {

	private final LineDecoder[] decoders;

	// -----------

	public LineDecodersChain(final LineDecoder... decoders) {
		checkNotNull(decoders);

		this.decoders = Arrays.copyOf(decoders, decoders.length);
	}

	private static final void checkNotNull(final LineDecoder... decoders) {
		for (final LineDecoder decoder: decoders) {
			if (decoder == null) {
				throw new IllegalArgumentException("decoder cannot be null");
			}
		}
	}

	// -----------

	@Override
	public LogLine decode(final long filePos, final String line) {
		for (final LineDecoder decoder: decoders) {
			final LogLine candidate = decoder.decode(filePos, line);
			if (candidate != null) {
				return candidate;
			}
		}
		return null;
	}

}