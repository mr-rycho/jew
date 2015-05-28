package pl.rychu.jew.gui;

import javax.swing.SwingUtilities;



public class ModelFacade {

	private final ListModelLog model;

	public ModelFacade(final ListModelLog listModelLog) {
		this.model = listModelLog;
	}

	// -------

	public void clear(final int version) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				model.clear(version, true);
			}
		});
	}

	public void addF(final long[] values, final int length) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				model.addF(values, length);
			}
		});
	}

	public void addB(final long[] values, final int length) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				model.addB(values, length);
			}
		});
	}

	public void setSourceSize(final long sourceSize) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				model.setSourceSize(sourceSize);
			}
		});
	}
}
