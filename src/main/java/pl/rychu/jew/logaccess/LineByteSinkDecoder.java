package pl.rychu.jew.logaccess;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

public class LineByteSinkDecoder implements LineByteSink {

	private ByteBuffer byteBuffer = ByteBuffer.allocate(1000);

	private CharBuffer charBuffer = CharBuffer.allocate(1000);

	private final CharsetDecoder decoder;

	private final LinePosSink linePosSink;

	// ----------

	public LineByteSinkDecoder(final LinePosSink linePosSink
	 , final String charsetName) {
		final CharsetDecoder decoder = Charset.forName(charsetName).newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPLACE);
		decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		decoder.replaceWith("?");

		this.decoder = decoder;
		this.linePosSink = linePosSink;
	}

	// ----------

	@Override
	public void put(final ByteBuffer buffer) {
		if (buffer.hasRemaining()) {
			prepareBuffers(buffer.remaining());
			byteBuffer.put(buffer);
		}
	}

	@Override
	public void lineBreak(final long offset) {
		final int length = byteBuffer.position();
		byteBuffer.flip();
		charBuffer.clear();
		final CoderResult coderResult = decoder.decode(byteBuffer, charBuffer, true);
		if (coderResult.isError() || coderResult.isMalformed()
		 || coderResult.isOverflow() || coderResult.isUnmappable()
		 || (coderResult.isUnderflow() && byteBuffer.hasRemaining())) {
			throw new IllegalStateException("coder result: "+coderResult);
		}
		if (byteBuffer.hasRemaining()) {
			throw new IllegalStateException();
		}

		final String line = new String(charBuffer.array(), 0, charBuffer.position());
		linePosSink.put(line, offset, length);
		byteBuffer.clear();
	}

	@Override
	public void reset() {
		byteBuffer.clear();
		charBuffer.clear();
		decoder.reset();
		linePosSink.reset();
	}

	// -------------------

	private void prepareBuffers(final int bytesToAdd) {
		if (bytesToAdd > byteBuffer.remaining()) {
			final int oldSize = byteBuffer.limit();
			final int newSize = Math.max(oldSize * 3 / 2 + 2
			 , bytesToAdd+oldSize);
			byteBuffer = copyOf(byteBuffer, newSize);
			charBuffer = copyOf(charBuffer, newSize);
		}
	}

	private static ByteBuffer copyOf(final ByteBuffer byteBuffer, final int newLength) {
		final ByteBuffer result = ByteBuffer.allocate(newLength);
		byteBuffer.flip();
		result.put(byteBuffer);
		return result;
	}

	private static CharBuffer copyOf(final CharBuffer charBuffer, final int newLength) {
		final CharBuffer result = CharBuffer.allocate(newLength);
		charBuffer.flip();
		result.put(charBuffer);
		return result;
	}

}
