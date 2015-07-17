package pl.rychu.jew.logaccess;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.gl.GrowingListVer;
import pl.rychu.jew.linedec.LineDecoder;
import pl.rychu.jew.logline.LogLine;

public class LogFileReader implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(LogFileReader.class);

	private final ByteBuffer byteBuffer = ByteBuffer.allocate(100_000);

	private final LogAccessFile logAccessFile;

	private final LineDecoder lineTypeRecognizer
	 = LineDecodersChainFactory.getLineDecodersChain();

	private final LinePosSink indexer;

	private final LineByteSink lineByteSink;

	private final LineDividerUtf8 lineDivider;

	private final Path path;

	private Object fileKey;

	private Long prevFileSize;

	private boolean isWindows;

	// --------

	public LogFileReader(LogAccessFile logAccessFile
	 , String pathStr, GrowingListVer<LogLine> index, boolean isWindows) {
		this.logAccessFile = logAccessFile;
		this.path = FileSystems.getDefault().getPath(pathStr);
		this.indexer = new Indexer(lineTypeRecognizer, index);
		this.lineByteSink = new LineByteSinkDecoder(indexer, "UTF-8");
		this.lineDivider = new LineDividerUtf8(lineByteSink);
		this.isWindows = isWindows;
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

		FileChannel fc = logAccessFile.getFileChannel();
		if (fc == null) {
			return 0L;
		} else {
			long result = 0L;
			try {
				while (true) {
					final int br = fc.read(byteBuffer);
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
		FileChannel fc = logAccessFile.getFileChannel();
		if (fc != null) {
			final BasicFileAttributes attrs = readAttributes();
			log.trace("attrs: {}", attrs);
			final Object fk = attrs!=null ? attrs.fileKey() : null;
			log.trace("fk: {}", fk);
			final Long fs = attrs!=null ? attrs.size() : null;
			log.trace("fs: {}", fs);
			boolean fileKeyChanged = !isWindows && (fk==null || !fk.equals(fileKey));
			boolean fileSizeChanged = fs==null || (prevFileSize!=null && fs<prevFileSize);
			log.trace("change: {} / {}", fileKeyChanged, fileSizeChanged);
			if (fileKeyChanged || fileSizeChanged) {
				log.debug("old fk = {}", fileKey);
				log.debug("new fk = {}", fk);
				closeFileChannel();
				byteBuffer.clear();
				lineDivider.reset();
				log.debug("list cleared");
			}
			prevFileSize = fs;
		}
	}

	private void closeFileChannel() {
		log.debug("will close file channel");
		final FileChannel fc = logAccessFile.getFileChannel();
		logAccessFile.resetFileChannel();
		if (fc == null) {
			log.debug("file channel is null");
		} else {
			try {
				fc.close();
			} catch(IOException e) {
				log.error("cannot close file", e);
			}
			fileKey = null;
		}
	}

	private void checkAndOpen() {
		if (logAccessFile.getFileChannel() == null) {
			tryOpen();
		}
	}

	private void tryOpen() {
		FileChannel fc = null;
		try {
			fc = FileChannel.open(path, StandardOpenOption.READ);
			fileKey = readAttributes().fileKey();
			log.debug("new file key is {}", fileKey);
		} catch (NoSuchFileException e) {
			fc = null;
		} catch (IOException e) {
			log.error("IOException", e);
			fc = null;
		}
		logAccessFile.setFileChannel(fc);
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
