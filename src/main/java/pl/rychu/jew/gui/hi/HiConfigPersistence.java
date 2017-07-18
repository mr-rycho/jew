package pl.rychu.jew.gui.hi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.jew.util.ColorUtil;
import pl.rychu.jew.util.FileUtil;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



class HiConfigPersistence {

	private static final Logger log = LoggerFactory.getLogger(HiConfigPersistence.class);

	private static final String[] ENV_HOMES = new String[]{"HOME", "USERPROFILE"};

	private static final String FILENAME = ".jew.hi";

	// ----------------

	private static String getFilename() {
		final String homeDir = getFirstEnv(ENV_HOMES);
		log.debug("homeDir = {}", homeDir);
		return (homeDir!=null?homeDir:".")+"/"+FILENAME;
	}

	private static String getFirstEnv(String... envKeys) {
		return Arrays.stream(envKeys).map(System::getenv)
		 .filter(Objects::nonNull).findFirst().orElse(null);
	}

	static HiConfig load() {
		final String filename = getFilename();
		log.debug("loading hi config from \"{}\"", filename);
		return load(filename);
	}

	static HiConfig load(final String filename) {
		try {
			return new HiConfig(FileUtil.loadLines(filename).stream().map(new ToHiConfigEntry())
			 .collect(Collectors.toList()));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	static void save(HiConfig hiConfig) {
		final String filename = getFilename();
		log.debug("saving hi config to \"{}\"", filename);
		save(hiConfig, filename);
	}


	static void save(HiConfig hiConfig, String filename) {
		Iterator<String> lineIterator = getEntries(hiConfig).stream()
		 .map(new HiConfigEntryToLine()).collect(Collectors.toList()).iterator();
		FileUtil.saveLines(lineIterator, filename);
	}

	// ===========

	private static class ToHiConfigEntry implements Function<String, HiConfigEntry> {

		private static final Pattern LINE_PATTERN = Pattern.compile(
		 "^"
		 +"[ \\t]*([0-9a-fA-F]+)[ \\t]*:"
		 +"[ \\t]*([0-9a-fA-F]+)[ \\t]*:"
		 +"(.*)"
		 +"$"
		);

		@Override
		public HiConfigEntry apply(final String line) {
			final Matcher matcher = LINE_PATTERN.matcher(line);
			if (!matcher.matches()) {
				throw new IllegalArgumentException("bad line: "+line);
			} else {
				return decode(matcher.group(1), matcher.group(2), matcher.group(3));
			}
		}

		private HiConfigEntry decode(final String colorBStr, final String colorFStr
		 , final String regexpStr) {
			return decode(ColorUtil.getColor(colorBStr), ColorUtil.getColor(colorFStr), regexpStr);
		}

		private HiConfigEntry decode(final int colorB, final int colorF, final String regexpStr) {
			return new HiConfigEntry(regexpStr, colorB, colorF);
		}

	}

	// ===========

	private static List<HiConfigEntry> getEntries(HiConfig hiConfig) {
		int size = hiConfig.size();
		List<HiConfigEntry> result = new ArrayList<>(size);
		for (int i=0; i<size; i++) {
			result.add(hiConfig.get(i));
		}
		return result;
	}

	private static class HiConfigEntryToLine implements Function<HiConfigEntry, String> {
		@Override
		public String apply(HiConfigEntry t) {
			String colorBStr = ColorUtil.toCssColor(t.getColorB());
			String colorFStr = ColorUtil.toCssColor(t.getColorF());
			return colorBStr+":"+colorFStr+":"+t.getRegexp();
		}
	}

}
