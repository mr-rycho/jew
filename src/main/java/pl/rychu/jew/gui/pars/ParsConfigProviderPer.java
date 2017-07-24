package pl.rychu.jew.gui.pars;

import java.util.Arrays;

public class ParsConfigProviderPer implements ParsConfigProvider {

	private final String filename;

	// ---

	public ParsConfigProviderPer() {
		this(null);
	}

	private ParsConfigProviderPer(String filename) {
		super();
		this.filename = filename;
	}

	// ---

	@Override
	public ParsConfig get() {
		ParsConfig parsConfig = filename != null ? ParsConfigPersistence.load(filename) :
		 ParsConfigPersistence.load();
		return parsConfig.size() != 0 ? parsConfig : createDefaultParsConfig();
	}

	@Override
	public void put(ParsConfig parsConfig) {
		if (filename != null) {
			ParsConfigPersistence.save(parsConfig, filename);
		} else {
			ParsConfigPersistence.save(parsConfig);
		}
	}

	private static ParsConfig createDefaultParsConfig() {
		String regexThread1 = "[^()]+";
		String regexThread2 = "[^()]*" + "\\(" + "[^)]*" + "\\)" + "[^()]*";
		String lineDecoderPattern = "^([-+:, 0-9]+)" + "[ \\t]+" + "([A-Z]+)" + "[ \\t]+" + "\\[" + ""
		 + "([^]]+)\\]" + "[ \\t]+" + "\\(" + "(" + regexThread1 + "|" + regexThread2 + ")" + "\\)" +
		 "[" + " \\t]+" + "(.*)$";
		ParsConfigEntry pcWildfly = new ParsConfigEntry("wildfly", lineDecoderPattern, 1, 2, 3, 4, 5);

		return new ParsConfig(Arrays.asList(pcWildfly));
	}

}
