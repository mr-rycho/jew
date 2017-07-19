package pl.rychu.jew.gui.pars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.jew.gui.util.CfgUtil;
import pl.rychu.jew.util.FileUtil;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


class ParsConfigPersistence {

	private static final Logger log = LoggerFactory.getLogger(ParsConfigPersistence.class);

	private static final String FILENAME = ".jew.pars";

	// ----------------

	static ParsConfig load() {
		String filename = CfgUtil.getFilename(FILENAME);
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
		String filename = CfgUtil.getFilename(FILENAME);
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
			return CfgUtil.escape(Arrays.asList(t.getName(), t.getGroupTime(), t.getGroupLevel(), t
			 .getGroupClass(), t.getGroupThread(), t.getGroupMessage(), t.getPattern()));
		}
	}

	private static class ToParsConfigEntry implements Function<String, ParsConfigEntry> {
		@Override
		public ParsConfigEntry apply(String line) {
			List<String> fields = CfgUtil.unescape(line);
			return new ParsConfigEntry(fields.get(0), fields.get(6), Integer.parseInt(fields.get(1)),
			 Integer.parseInt(fields.get(2)), Integer.parseInt(fields.get(3)), Integer.parseInt(fields
			 .get(4)), Integer.parseInt(fields.get(5)));
		}
	}

}
