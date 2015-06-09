package pl.rychu.jew.gui.hi;

public class HiConfigProviderPer implements HiConfigProvider {

	private final String filename;

	// ---

	public HiConfigProviderPer() {
		this(null);
	}

	public HiConfigProviderPer(final String filename) {
		super();
		this.filename = filename;
	}

	// ---

	@Override
	public HiConfig get() {
		return filename!=null ? HiConfigPersistence.load(filename) : HiConfigPersistence.load();
	}

	@Override
	public void put(HiConfig hiConfig) {
		if (filename != null) {
			HiConfigPersistence.save(hiConfig, filename);
		} else {
			HiConfigPersistence.save(hiConfig);
		}
	};

}
