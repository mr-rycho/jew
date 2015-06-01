package pl.rychu.jew.gui.hi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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
			return new HiConfig(loadLines(filename).stream().map(new ToHiConfigEntry())
			 .collect(Collectors.toList()));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static List<String> loadLines(final String filename) throws IOException {
		final File file = new File(filename);

		if (!file.exists()) {
			return Collections.emptyList();
		} else {
			return loadLines(file);
		}
	}

	private static List<String> loadLines(final File file) throws IOException {
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
			return decode(getColor(colorBStr), getColor(colorFStr), regexpStr);
		}

		private HiConfigEntry decode(final int colorB, final int colorF, final String regexpStr) {
			return new HiConfigEntry(regexpStr, colorB, colorF);
		}

		private int getColor(final String str) {
			if (str.length() == 3) {
				return getColor3(str);
			} else if (str.length() == 6) {
					return getColor6(str);
			} else {
				throw new IllegalStateException("expecting color, got: "+str);
			}
		}

		private int getColor3(final String str) {
			if (str.length() != 3) {
				throw new IllegalStateException("expecting 3 hex digits, got: "+str);
			}
			final char c1 = str.charAt(0);
			final char c2 = str.charAt(1);
			final char c3 = str.charAt(2);
			return getColor6(""+c1+c1+c2+c2+c3+c3);
		}

		private int getColor6(final String str) {
			if (str.length() != 6) {
				throw new IllegalStateException("expecting 6 hex digits, got: "+str);
			}
			return readHex(str);
		}

		private int readHex(final String str) {
			int num = 0;
			final int len = str.length();
			for (int i=0; i<len; i++) {
				final char c = str.charAt(i);
				num = (num<<4) | readHexDigit(c);
			}
			return num;
		}

		private int readHexDigit(final char c) {
			if (c>='0' && c<='9') {
				return (int)(c - '0');
			} else if (c>='a' && c<='f') {
				return 10 + (int)(c - 'a');
			} else if (c>='A' && c<='F') {
				return 10 + (int)(c - 'A');
			} else {
				throw new IllegalStateException("incorrect hex digit: "+c);
			}
		}

	}

}
