package pl.rychu.jew.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FileUtil {

	private final static Logger log = LoggerFactory.getLogger(FileUtil.class);


	public static Map<String, Object> getFileAttrs(final Path path) {
		try {
			return Files.readAttributes(path, "posix:*", LinkOption.NOFOLLOW_LINKS);
		} catch (IOException e) {
			log.error("error: {} / {}", e.getClass().getCanonicalName(), e.getMessage());
			return null;
		}
	}

	public static void logFileAttrs(final Map<String, Object> attrs) {
		if (attrs == null) {
			log.debug("attrs is null");
		} else {
			log.debug("attrs ({}):", attrs.size());
			for (final Entry<String, Object> entry: attrs.entrySet()) {
				final String key = entry.getKey();
				final Object val = entry.getValue();
				final String valClass = val==null ? "[null]" : val.getClass().getCanonicalName();
				log.debug("  {} -> {} / {}", key, val, valClass);
			}
		}
	}

}
