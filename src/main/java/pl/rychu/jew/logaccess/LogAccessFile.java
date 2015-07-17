package pl.rychu.jew.logaccess;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.gl.GrowingListVer;
import pl.rychu.jew.gl.GrowingListVerLocked;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;



public class LogAccessFile implements LogAccess {

	private static final Logger log = LoggerFactory.getLogger(LogAccessFile.class);

	private final GrowingListVer<LogLine> index = GrowingListVerLocked.create(65536);

	private volatile FileChannel fileChannel;

	private final Queue<LineReaderDecoder> readerPool
	 = new ConcurrentLinkedQueue<>();

	// ------------------

	private LogAccessFile() {}

	public static LogAccess create(final String pathStr, boolean isWindows) {
		final LogAccessFile result = new LogAccessFile();

		LogFileReader logFileReader = new LogFileReader(result, pathStr, result.index, isWindows);
		new Thread(logFileReader).start();

		return result;
	}

	// ---

	FileChannel getFileChannel() {
		return fileChannel;
	}

	void resetFileChannel() {
		fileChannel = null;
	}

	void setFileChannel(FileChannel fc) {
		fileChannel = fc;
	}

	// ---

	@Override
	public int getVersion() {
		return index.getVersion();
	}

	@Override
	public long size(final int version) throws BadVersionException {
		return index.size(version);
	}

	@Override
	public LogLine get(final long pos, final int version) throws BadVersionException {
		return index.get(pos, version);
	}

	@Override
	public LogLineFull getFull(final long pos, final int version) throws BadVersionException {
		final FileChannel fc = fileChannel;
		if (fc == null) {
			return null;
		}

		// TODO apply cache here

		final LogLine logLine = getOrNull(pos, version);
		if (logLine == null) {
			return null;
		}

		LineReaderDecoder lineReadDec = readerPool.poll();
		if (lineReadDec == null) {
			log.debug("allocate decoder");
			lineReadDec = new LineReaderDecoder();
		}
		try {
			return lineReadDec.readFull(fc, logLine);
		} finally {
			readerPool.add(lineReadDec);
		}
	}

	private LogLine getOrNull(final long pos, final int version) throws BadVersionException {
		try {
			return index.get(pos, version);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	// ==================

	private static class LineReaderDecoder {

		private ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
		private CharBuffer charBuffer = CharBuffer.allocate(1000);
		private final CharsetDecoder decoder;

		LineReaderDecoder() {
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
}
