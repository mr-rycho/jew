package pl.rychu.jew.logaccess;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;



public class LineReaderDecoder {

	private ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
	private CharBuffer charBuffer = CharBuffer.allocate(1000);
	private final CharsetDecoder decoder;

	public LineReaderDecoder() {
		final Charset charset = Charset.forName("UTF-8");
		decoder = charset.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPLACE);
		decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		decoder.replaceWith("?");
	}

	LogLineFull readFull(final FileChannel fc, final LogLine logLine) {
		final long linePos = logLine.getFilePos();
		final int lineLen = logLine.getLength();
		byteBuffer.clear();
		charBuffer.clear();
		if (byteBuffer.remaining() < lineLen) {
			byteBuffer = ByteBuffer.allocate(lineLen);
			charBuffer = CharBuffer.allocate(lineLen);
		}

		byteBuffer.position(byteBuffer.limit() - lineLen);
		final int mark = byteBuffer.position();
		try {
			while (byteBuffer.hasRemaining()) {
				final int br = fc.read(byteBuffer, linePos + byteBuffer.position() - mark);
				if (br < 0) {
					return null;
				}
			}
		} catch (IOException e) {
			return null;
		}

		decoder.reset();
		byteBuffer.position(mark);
		decoder.decode(byteBuffer, charBuffer, true);

		final String line = new String(charBuffer.array(), 0, charBuffer.position());
		return new LogLineFull(logLine, line);
	}
}
