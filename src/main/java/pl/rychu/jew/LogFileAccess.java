package pl.rychu.jew;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import pl.rychu.jew.GrowingList;
import pl.rychu.jew.LogLine;
import pl.rychu.jew.linedec.LineDecoder;

public class LogFileAccess {

	private final String pathStr;

	private final GrowingList<LogLine> index = new GrowingListLocked<>(1024);

	private final LineDecoder lineTypeRecognizer
	 = LineDecodersChainFactory.getLineDecodersChain();

	private final LinePosSink indexer = new Indexer(lineTypeRecognizer, index);

	final LineByteSink lineByteSink = new LineByteSinkDecoder(indexer, "UTF-8");

	final LineDividerUtf8 lineDivider = new LineDividerUtf8(lineByteSink);

	// ------------------

	public LogFileAccess(final String pathStr) {
		this.pathStr = pathStr;

		new Thread(new LogFileReader()).start();
	}

	public long size() {
		return index.size();
	}

	public LogLine get(final long pos) {
		return index.get(pos);
	}

	// ==================

	private class LogFileReader implements Runnable {

		private FileChannel fileChannel;

		private final ByteBuffer byteBuffer = ByteBuffer.allocate(100_000);

		// --------

		private void tryOpen() {
			final Path path = FileSystems.getDefault().getPath(pathStr);
			try {
				fileChannel = FileChannel.open(path, StandardOpenOption.READ);
			} catch (NoSuchFileException e) {
				fileChannel = null;
			} catch (IOException e) {
				e.printStackTrace();
				fileChannel = null;
			}
		}

		private void checkAndOpen() {
			if (fileChannel == null) {
				tryOpen();
			}
		}

		public long read() {
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

		@Override
		public void run() {
			while (true) {
				read();
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					System.out.println("shutting down");
					break;
				}
			}
		}

	}

}
