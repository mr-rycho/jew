package pl.rychu.jew.util;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ArgChk implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(ArgChk.class);

	private final Path path;

	private ArgChk(final String pathStr) {
		this.path = FileSystems.getDefault().getPath(pathStr);
	}

	@Override
	public void run() {
		while (true) {
			final Map<String, Object> attrs = FileUtil.getFileAttrs(path);

			log.debug("-------");
			FileUtil.logFileAttrs(attrs);
			log.debug("-------");

			sleep(4000L);
		}
	}

	private static void sleep(final long ms) {
		try {
			Thread.sleep(4000L);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
