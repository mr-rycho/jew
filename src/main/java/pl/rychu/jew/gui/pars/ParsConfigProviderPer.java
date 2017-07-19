package pl.rychu.jew.gui.pars;

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
		return filename!=null ? ParsConfigPersistence.load(filename) : ParsConfigPersistence.load();
	}

	@Override
	public void put(ParsConfig parsConfig) {
		if (filename != null) {
			ParsConfigPersistence.save(parsConfig, filename);
		} else {
			ParsConfigPersistence.save(parsConfig);
		}
	}

}
