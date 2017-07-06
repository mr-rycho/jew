package pl.rychu.jew.logaccess;

import pl.rychu.jew.gl.GrowingListVer;
import pl.rychu.jew.linedec.LineDecoder;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLine.LogLineType;
import pl.rychu.jew.logline.LogLineFull;

public class Indexer implements LinePosSink {

	private final GrowingListVer<LogLine> index;

	private final LineDecoder lineDecoder;

	private final LogAccessCache logReaderCache;

	private LogLine prevLogLine = null;

	private long lineNumber = 0L;

	// ------------------

	public Indexer(final LineDecoder lineDecoder
	 , final GrowingListVer<LogLine> index, LogAccessCache logReaderCache) {
		this.lineDecoder = lineDecoder;
		this.index = index;
		this.logReaderCache = logReaderCache;
	}

	@Override
	public boolean put(String line, long filePos, int length) {
		LogLine logLine = lineDecoder.decode(filePos, line, length, prevLogLine);
		if (logLine.getLogLineType()==LogLineType.TEXT && logLine.getLength()==0) {
			return false;
		}
		index.add(logLine);
		if (logReaderCache != null) {
			logReaderCache.write(lineNumber, new LogLineFull(logLine, line)
			 , index.getVersion());
		}
		lineNumber++;
		prevLogLine = logLine;
		return true;
	}

	@Override
	public void reset() {
		index.clear();
		lineNumber = 0L;
		prevLogLine = null;
	}

	protected LogLine getPrevLogLine() {
		return prevLogLine;
	}

}