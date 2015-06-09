package pl.rychu.jew.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

	public static List<String> loadLines(final String filename) throws IOException {
		final File file = new File(filename);

		if (!file.exists()) {
			return Collections.emptyList();
		} else {
			return loadLines(file);
		}
	}

	public static List<String> loadLines(final File file) throws IOException {
		final FileReader fr = new FileReader(file);
		try {
			final BufferedReader br = new BufferedReader(fr);
			try {
				String line = null;
				final List<String> result = new ArrayList<>();
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					result.add(line);
				}
				return result;
			} finally {
				br.close();
			}
		} finally {
			fr.close();
		}
	}

	// --------

	public static void saveLines(Iterator<String> lines, String filename) {
		try {
			saveLines(lines, new File(filename));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveLines(Iterator<String> lines, File file) throws IOException {
		try (FileWriter fw = new FileWriter(file)) {
			try (BufferedWriter bw = new BufferedWriter(fw)) {
				while (lines.hasNext()) {
					bw.write(lines.next());
					bw.newLine();
				}
			}
		}
	}

}
