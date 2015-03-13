package pl.rychu.jew;

import java.util.Arrays;

import pl.rychu.jew.LogLine.LogLineType;
import pl.rychu.jew.linedec.LineDecoder;

public class Indexer implements LinePosSink {

	private final GrowingList<LogLine> index;

	private final LineDecoder lineDecoder;

	private final long[] typeCounter = new long[LogLineType.values().length];

	public Indexer(final LineDecoder lineDecoder
	 , final GrowingList<LogLine> index) {
		this.lineDecoder = lineDecoder;
		this.index = index;
	}

	@Override
	public void put(String line, long filePos) {
		final LogLine logLine = lineDecoder.decode(filePos, line);
		index.add(logLine);
		final LogLineType logLineType = logLine.getLogLineType();
		typeCounter[logLineType.ordinal()]++;
	}

	public long[] getTypeCounts() {
		return Arrays.copyOf(typeCounter, typeCounter.length);
	}
}