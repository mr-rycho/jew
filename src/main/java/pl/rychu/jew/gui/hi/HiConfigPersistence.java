package pl.rychu.jew.gui.hi;

import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.util.ColorUtil;
import pl.rychu.jew.util.FileUtil;



public class HiConfigPersistence {

	private static final Logger log = LoggerFactory.getLogger(HiConfigPersistence.class);

	private static final String ENV_HOME = "HOME";

	private static final String FILENAME = ".jew.hi";

	// ----------------

	public static HiConfig load() {
		final String homeDir = System.getenv(ENV_HOME);
		final String file = (homeDir!=null?homeDir:".")+"/"+FILENAME;
		log.debug("loading hi config from \"{}\"", file);
		return load(file);
	}

	public static HiConfig load(final String filename) {
		try {
			return new HiConfig(FileUtil.loadLines(filename).stream().map(new ToHiConfigEntry())
			 .collect(Collectors.toList()));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
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

}
