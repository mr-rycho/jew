package pl.rychu.jew;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.rychu.jew.LogLine.LogLineType;
import pl.rychu.jew.linedec.LineDecoder;
import pl.rychu.jew.linedec.LogElemsCache;



public class ComboRunner {

	private static final ByteBuffer byteBuffer = ByteBuffer.allocate(1_000); // TODO 100_000);

	private static final String pathStr = "/home/rycho/Pulpit/server.log";

	// -------------

	public static void main(String... args) {

		final GrowingList<LogLine> index = new GrowingListLocked<>(1_024);

		final LineDecoder lineDecoder = LineDecodersChainFactory.getLineDecodersChain();

		final Indexer linePosSink = new Indexer(lineDecoder, index);
		// final LinePosSink linePosSink = new LinePosSinkNull();

		final LineByteSink lineByteSink = new LineByteSinkDecoder(linePosSink, "UTF-8");

		final LineDividerUtf8 lineDivider = new LineDividerUtf8(lineByteSink);

		final long t0 = System.currentTimeMillis();

		final Path path = FileSystems.getDefault().getPath(pathStr);
		try {
			try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
				long br = 0L;
				while (true) {
					final int read = channel.read(byteBuffer);
					if (read < 0) {
						System.out.println("eof; size = "+br);
						break;
					}
					if (read != byteBuffer.position()) {
						throw new IllegalStateException("read="+read+"; pos="+byteBuffer.position());
					}
					br += read;
					byteBuffer.flip();
					lineDivider.put(byteBuffer);
					byteBuffer.compact();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("index: "+index.size());
		final long t1 = System.currentTimeMillis();
		System.out.println("time: "+(t1-t0)+"ms");

		final long[] typeCounts = linePosSink.getTypeCounts();
		for (LogLineType logLineType: LogLineType.values()) {
			System.out.println(""+logLineType+": "+typeCounts[logLineType.ordinal()]);
		}

		System.out.println("threads: "+LogElemsCache.getThreadCount());

		{
			final String threadname
			 = "Thread-1 (HornetQ-client-global-threads-1332939477)";
			final List<LogLine> lines4 = new ArrayList<>(1000);
			final long t3 = System.currentTimeMillis();
			final long indexSize = index.size();
			for (long i=0L; i<indexSize; i++) {
				final LogLine logLine = index.get(i);
				if (logLine.getLogLineType() == LogLineType.STANDARD) {
					if (logLine.getThreadName().equals(threadname)) {
						lines4.add(logLine);
					}
				}
			}
			final long t4 = System.currentTimeMillis();
			System.out.println(""+lines4.size()+" lines in "+(t4-t3)+"ms");
		}

	}

	// ==========

	private static class Indexer implements LinePosSink {

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

}
