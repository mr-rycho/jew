package pl.rychu.jew.logaccess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.gl.GrowingListVer;
import pl.rychu.jew.gl.GrowingListVerLocked;
import pl.rychu.jew.linedec.LineDecoderCfg;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;

import java.nio.channels.FileChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class LogAccessFile implements LogAccess {

	private static final Logger log = LoggerFactory.getLogger(LogAccessFile.class);

	private final String encoding;

	private final GrowingListVer<LogLine> index = GrowingListVerLocked.create(65536);

	private volatile FileChannel fileChannel;

	private LogFileReader logFileReader;

	private final LogAccessCache logAccessCache = new LogAccessCache();
	private final LogAccessCache logReaderCache = new LogAccessCache();

	private final Queue<LineReaderDecoder> readerPool = new ConcurrentLinkedQueue<>();

	private long countAccess = 0L;
	private long countCacheViewHit = 0L;
	private long countCacheReadHit = 0L;
	private long countCacheMiss = 0L;

	// ------------------

	private LogAccessFile(String encoding) {
		this.encoding = encoding;
	}

	public static LogAccess create(final String pathStr, boolean isWindows,
	 LineDecoderCfg lineDecoderCfg) {
		String encoding = isWindows ? "windows-1250" : "UTF-8";
		log.debug("isWindows={};  encoding={}", isWindows, encoding);

		LogAccessFile result = new LogAccessFile(encoding);

		LogFileReader logFileReader = new LogFileReader(result, pathStr, result.index, isWindows,
		 encoding, lineDecoderCfg, result.logReaderCache);

		result.logFileReader = logFileReader;

		new Thread(logFileReader, "log-file-reader-thread").start();

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
	public void reconfig(LineDecoderCfg lineDecoderCfg) {
		logFileReader.reconfig(lineDecoderCfg);
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

		if ((countAccess % 1000) == 0) {
			log.trace("acc:{};  view:{};  read:{};  miss:{}", countAccess, countCacheViewHit,
			 countCacheReadHit, countCacheMiss);
		}

		countAccess++;
		LogLineFull lineFromCache = logAccessCache.read(pos, version);
		if (lineFromCache != null) {
			countCacheViewHit++;
			return lineFromCache;
		}

		lineFromCache = logReaderCache.read(pos, version);
		if (lineFromCache != null) {
			countCacheReadHit++;
			return lineFromCache;
		}

		countCacheMiss++;

		final LogLine logLine = getOrNull(pos, version);
		if (logLine == null) {
			return null;
		}

		LineReaderDecoder lineReadDec = readerPool.poll();
		if (lineReadDec == null) {
			log.debug("allocate decoder ({})", encoding);
			lineReadDec = new LineReaderDecoder(encoding);
		}
		try {
			LogLineFull result = lineReadDec.readFull(fc, logLine);
			logAccessCache.write(pos, result, version);
			return result;
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

}
