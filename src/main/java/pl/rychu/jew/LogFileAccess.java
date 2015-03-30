package pl.rychu.jew;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.gl.GrowingList;
import pl.rychu.jew.gl.GrowingListLocked;
import pl.rychu.jew.linedec.LineDecoder;



public class LogFileAccess {

	static final Logger log = LoggerFactory.getLogger(LogFileAccess.class);

	final String pathStr;

	private final GrowingList<LogLine> index = GrowingListLocked.create(1024);

	private volatile FileChannel fileChannel;

	private ByteBuffer byteBuffer = ByteBuffer.allocate(1000);

	private CharBuffer charBuffer = CharBuffer.allocate(1000);

	private final CharsetDecoder decoder;

	private final CopyOnWriteArrayList<LogFileListener> listeners
	 = new CopyOnWriteArrayList<>();

	// ------------------

	private LogFileAccess(final String pathStr) {
		this.pathStr = pathStr;

		final Charset charset = Charset.forName("UTF-8");
		decoder = charset.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.REPLACE);
		decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		decoder.replaceWith("?");

		new Thread(new LogFileReader()).start();
	}

	public static LogFileAccess create(final String pathStr) {
		final LogFileAccess result = new LogFileAccess(pathStr);

		result.index.addListener(result.new Listener());

		return result;
	}

	// ---

	public void addLogFileListener(final LogFileListener l) {
		listeners.add(l);
	}

	public void removeLogFileListener(final LogFileListener l) {
		listeners.remove(l);
	}

	// ---

	public long size() {
		return index.size();
	}

	public LogLine get(final long pos) {
		return index.get(pos);
	}

	public LogLineFull getFull(final long pos) {
		final FileChannel fc = fileChannel;
		if (fc == null) {
			return null;
		}

		// TODO apply cache here

		final LogLine logLine = getOrNull(pos);
		if (logLine == null) {
			return null;
		}
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

	private LogLine getOrNull(final long pos) {
		try {
			return index.get(pos);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	// ==================

	private class Listener implements IndexListener {
		@Override
		public void lineAdded() {
			for (final LogFileListener li: listeners) {
				li.linesAdded();
			}
		}

		@Override
		public void indexWasReset() {
			for (final LogFileListener li: listeners) {
				li.fileWasReset();
			}
		}
	}

	// ==================

	private class LogFileReader implements Runnable {

		private final ByteBuffer byteBuffer = ByteBuffer.allocate(100_000);

		private final LineDecoder lineTypeRecognizer
		 = LineDecodersChainFactory.getLineDecodersChain();

		private final LinePosSink indexer = new Indexer(lineTypeRecognizer, index);

		private final LineByteSink lineByteSink
		 = new LineByteSinkDecoder(indexer, "UTF-8");

		private final LineDividerUtf8 lineDivider
		 = new LineDividerUtf8(lineByteSink);

		private final Path path;

		private Object fileKey;

		private Long prevFileSize;

		private LogFileReader() {
			this.path = FileSystems.getDefault().getPath(pathStr);
		}

		// --------

		@Override
		public void run() {
			while (true) {
				read();
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					log.info("shutting down");
					break;
				}
			}
		}

		public long read() {
			checkAndClose();

			checkAndOpen();

			if (fileChannel == null) {
				return 0L;
			} else {
				long result = 0L;
				try {
					while (true) {
						final int br = fileChannel.read(byteBuffer);
						if (br < 0) {
							break;
						}
						result += br;
						byteBuffer.flip();
						lineDivider.put(byteBuffer);
						byteBuffer.compact();
					}
					return result;
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}

		private void checkAndClose() {
			if (fileChannel != null) {
				final BasicFileAttributes attrs = readAttributes();
				final Object fk = attrs!=null ? attrs.fileKey() : null;
				final Long fs = attrs!=null ? attrs.size() : null;
				if (fk==null || !fk.equals(fileKey) || fs==null
				 || (prevFileSize!=null && fs<prevFileSize)) {
					log.debug("old fk = {}", fileKey);
					log.debug("new fk = {}", fk);
					fileChannel = null;
					fileKey = null;
					index.clear();
					log.debug("list cleared");
				}
				prevFileSize = fs;
			}
		}

		private void checkAndOpen() {
			if (fileChannel == null) {
				tryOpen();
			}
		}

		private void tryOpen() {
			try {
				fileChannel = FileChannel.open(path, StandardOpenOption.READ);
				fileKey = readAttributes().fileKey();
				log.debug("new file key is {}", fileKey);
			} catch (NoSuchFileException e) {
				fileChannel = null;
			} catch (IOException e) {
				log.error("IOException", e);
				fileChannel = null;
			}
		}

		private BasicFileAttributes readAttributes() {
			try {
				return Files.readAttributes(path
				 , BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			} catch (IOException e) {
				return null;
			}
		}

	}

}
