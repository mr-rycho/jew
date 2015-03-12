package pl.rychu.jew;

import java.nio.ByteBuffer;

public class LineDividerUtf8 {

	private final LineByteSink lineByteSink;

	private long currentLinePos = 0L;

	private long currentPos = 0L;

	private boolean skipCrLf = false;

	// -----------

	public LineDividerUtf8(final LineByteSink lineByteSink) {
		this.lineByteSink = lineByteSink;
	}

	// -----------

	public void put(final ByteBuffer byteBuffer) {
		final ByteBuffer view = byteBuffer.duplicate();
		while (byteBuffer.hasRemaining()) {
			final byte b = byteBuffer.get();
			currentPos++;
			if (b=='\n' || b=='\r') {
				if (!skipCrLf) {
					view.limit(byteBuffer.position()-1);
					if (view.hasRemaining()) {
						lineByteSink.put(view);
					}
					lineByteSink.lineBreak(currentLinePos);
				}
				view.limit(byteBuffer.limit());
				view.position(byteBuffer.position());
				currentLinePos = currentPos;
				skipCrLf = true;
			} else {
				skipCrLf = false;
			}
		}
		if (view.hasRemaining()) {
			lineByteSink.put(view);
		}
	}

}
