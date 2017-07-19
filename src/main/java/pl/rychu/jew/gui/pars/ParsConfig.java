package pl.rychu.jew.gui.pars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created on 18.07.2017.
 */
public class ParsConfig {

	private final List<ParsConfigEntry> entries = new ArrayList<>();

	// ----------

	public ParsConfig(Collection<ParsConfigEntry> entries) {
		this.entries.addAll(entries);
	}

	static ParsConfig clone(ParsConfig parsConfig) {
		return new ParsConfig((parsConfig.entries));
	}

	// ----------

	public int size() {
		return entries.size();
	}

	public ParsConfigEntry get(int index) {
		return entries.get(index);
	}

	public List<ParsConfigEntry> getEntries() {
		return Collections.unmodifiableList(entries);
	}

}
