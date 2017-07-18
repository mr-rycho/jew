package pl.rychu.jew.gui.hi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



public class HiConfig {

	private final List<HiConfigEntry> entries = new ArrayList<>();

	// -----------

	HiConfig(final Collection<HiConfigEntry> entries) {
		this.entries.addAll(entries);
	}

	static HiConfig clone(HiConfig hiConfig) {
		return new HiConfig(hiConfig.entries);
	}

	// -----------

	public void add(final HiConfigEntry entry) {
		entries.add(entry);
	}

	public int size() {
		return entries.size();
	}

	public HiConfigEntry get(final int index) {
		return entries.get(index);
	}

	public void remove(final int index) {
		entries.remove(index);
	}

	public void replace(final int index, final HiConfigEntry entry) {
		entries.set(index, entry);
	}

}
