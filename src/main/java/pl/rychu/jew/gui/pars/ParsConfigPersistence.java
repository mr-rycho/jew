package pl.rychu.jew.gui.pars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.jew.util.FileUtil;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


class ParsConfigPersistence {

	private static final Logger log = LoggerFactory.getLogger(ParsConfigPersistence.class);

	private static final String[] ENV_HOMES = new String[]{"HOME", "USERPROFILE"};

	private static final String FILENAME = ".jew.pars";

	// ----------------

	private static String getFilename() {
		String homeDir = getFirstEnv(ENV_HOMES);
		log.debug("homeDir = {}", homeDir);
		return (homeDir != null ? homeDir : ".") + "/" + FILENAME;
	}

	private static String getFirstEnv(String... envKeys) {
		return Arrays.stream(envKeys).map(System::getenv).filter(Objects::nonNull).findFirst().orElse
		 (null);
	}

	static ParsConfig load() {
		String filename = getFilename();
		log.debug("loading pars config from \"{}\"", filename);
		return load(filename);
	}

	static ParsConfig load(String filename) {
		try {
			return new ParsConfig(FileUtil.loadLines(filename).stream().map(new ToParsConfigEntry())
			 .collect(Collectors.toList()));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	static void save(ParsConfig parsConfig) {
		String filename = getFilename();
		log.debug("saving pars config to \"{}\"", filename);
		save(parsConfig, filename);
	}


	static void save(ParsConfig parsConfig, String filename) {
		Iterator<String> lineIterator = getEntries(parsConfig).stream().map(new ParsConfigEntryToLine
		 ()).collect(Collectors.toList()).iterator();
		FileUtil.saveLines(lineIterator, filename);
	}

	// ===========

	private static List<ParsConfigEntry> getEntries(ParsConfig parsConfig) {
		int size = parsConfig.size();
		List<ParsConfigEntry> result = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			result.add(parsConfig.get(i));
		}
		return result;
	}

	private static class ParsConfigEntryToLine implements Function<ParsConfigEntry, String> {
		@Override
		public String apply(ParsConfigEntry t) {
			return escape(Arrays.asList(t.getName(), t.getGroupTime(), t.getGroupLevel(), t
			 .getGroupClass(), t.getGroupThread(), t.getGroupMessage(), t.getPattern()));
		}
	}

	private static class ToParsConfigEntry implements Function<String, ParsConfigEntry> {
		@Override
		public ParsConfigEntry apply(String line) {
			List<String> fields = unescape(line);
			return new ParsConfigEntry(fields.get(0), fields.get(6), Integer.parseInt(fields.get(1)),
			 Integer.parseInt(fields.get(2)), Integer.parseInt(fields.get(3)), Integer.parseInt(fields
			 .get(4)), Integer.parseInt(fields.get(5)));
		}
	}

	private static String escape(Collection<Object> strs) {
		return strs.stream().map(o -> o != null ? o.toString() : "").map
		 (ParsConfigPersistence::escape).collect(Collectors.joining(":"));
	}

	private static String escape(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (c == '\n') {
				sb.append("\\n");
			} else {
				if (c == '\\' || c == ':') {
					sb.append("\\");
				}
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private static List<String> unescape(String s) {
		List<String> result = new ArrayList<>();
		StringBuilder field = new StringBuilder();
		int len = s.length();
		int index = 0;
		while (index < len) {
			char c = s.charAt(index++);
			if (c == '\\') {
				char b = s.charAt(index++);
				if (b == 'n') {
					field.append('\n');
				} else {
					field.append(b);
				}
			} else if (c == ':') {
				result.add(field.toString());
				field.setLength(0);
			} else {
				field.append(c);
			}
		}
		result.add(field.toString());
		return result;
	}

}
