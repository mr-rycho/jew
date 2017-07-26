package pl.rychu.jew.gui.pars;

import java.util.Collections;

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
		String regexThread = "\\(" + "(" + regexThread1 + "|" + regexThread2 + ")" + "\\)";
		//noinspection StringBufferReplaceableByString
		StringBuilder pb = new StringBuilder();
		pb.append("^([-+:, 0-9]+)").append("\n");
		pb.append("[ \\t]+").append("([A-Z]+)").append("\n");
		pb.append("[ \\t]+").append("\\[([^]]+)\\]").append("\n");
		pb.append("[ \\t]+").append(regexThread).append("\n");
		pb.append("[ \\t]+").append("(.*)$");

		ParsConfigEntry pcWildfly = new ParsConfigEntry("wildfly", pb.toString(), 1, 2, 3, 4, 5);

		return new ParsConfig(Collections.singletonList(pcWildfly));
	}

}
