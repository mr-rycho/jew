package pl.rychu.jew.logaccess;

import java.util.Arrays;

import pl.rychu.jew.gl.GrowingListVer;
import pl.rychu.jew.linedec.LineDecoder;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLine.LogLineType;

public class Indexer implements LinePosSink {

	private final GrowingListVer<LogLine> index;

	private final LineDecoder lineDecoder;

	private final long[] typeCounter = new long[LogLineType.values().length];

	private LogLine prevLogLine = null;

	// ------------------

	public Indexer(final LineDecoder lineDecoder
	 , final GrowingListVer<LogLine> index) {
		this.lineDecoder = lineDecoder;
		this.index = index;
	}

	@Override
	public void put(String line, long filePos, int length) {
		final LogLine logLine = lineDecoder.decode(filePos, line, length, prevLogLine);
		index.add(logLine);
		final LogLineType logLineType = logLine.getLogLineType();
		typeCounter[logLineType.ordinal()]++;
		prevLogLine = logLine;
	}

	@Override
	public void reset() {
		index.clear();
		prevLogLine = null;
	}

	public long[] getTypeCounts() {
		return Arrays.copyOf(typeCounter, typeCounter.length);
	}
}