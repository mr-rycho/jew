package pl.rychu.jew;

import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.gl.GrowingListVer;
import pl.rychu.jew.gl.GrowingListVerLocked;
import pl.rychu.jew.linedec.LineDecoder;
import pl.rychu.jew.linedec.LineDecoderCfg;
import pl.rychu.jew.linedec.LineDecoderFull;
import pl.rychu.jew.linedec.LogElemsCache;
import pl.rychu.jew.logaccess.*;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLine.LogLineType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;



public class ComboRunner {

	private static final ByteBuffer byteBuffer = ByteBuffer.allocate(1_000); // TODO 100_000);

	private static final String pathStr = "/home/rycho/Pulpit/server.log";

	// -------------

	public static void main(String... args) throws BadVersionException {

		final GrowingListVer<LogLine> index = GrowingListVerLocked.create(1_024);

		String regexThread1 = "[^()]+";
		String regexThread2 = "[^()]*"+"\\("+"[^)]*"+"\\)"+"[^()]*";
		String lineDecoderPattern = "^([-+:, 0-9]+)"
		 +"[ \\t]+"+"([A-Z]+)"
		 +"[ \\t]+"+"\\[([^]]+)\\]"
		 +"[ \\t]+"+"\\("+"("+regexThread1+"|"+regexThread2+")"+"\\)"
		 +"[ \\t]+"+"(.*)$";
		LineDecoderCfg lineDecoderCfg = new LineDecoderCfg(Pattern.compile(lineDecoderPattern)
		 , 1, 2, 3, 4, 5);

		LineDecoder lineDecoder = new LineDecoderFull(lineDecoderCfg);

		IndexerWithTypes linePosSink = new IndexerWithTypes(lineDecoder, index, null);

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

		System.out.println("index: "+index.size(index.getVersion()));
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
			final long indexSize = index.size(index.getVersion());
			for (long i=0L; i<indexSize; i++) {
				final LogLine logLine = index.get(i, index.getVersion());
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

	// ===========

	private static class IndexerWithTypes extends Indexer {
		private final long[] typeCounter = new long[LogLineType.values().length];

		public IndexerWithTypes(final LineDecoder lineDecoder, final GrowingListVer<LogLine> index,
		 LogAccessCache logReaderCache) {
			super(lineDecoder, index, logReaderCache);
		}

		@Override
		public boolean put(String line, long filePos, int length) {
			boolean put = super.put(line, filePos, length);
			if (put) {
				LogLine prev = getPrevLogLine();
				if (prev != null) {
					LogLineType logLineType = prev.getLogLineType();
					typeCounter[logLineType.ordinal()]++;
				}
			}
			return put;
		}

		public long[] getTypeCounts() {
			return Arrays.copyOf(typeCounter, typeCounter.length);
		}
	}

}
