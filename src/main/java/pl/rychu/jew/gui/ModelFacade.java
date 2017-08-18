package pl.rychu.jew.gui;

import javax.swing.*;


public class ModelFacade {

	private final ListModelLog model;

	ModelFacade(ListModelLog listModelLog) {
		this.model = listModelLog;
	}

	// -------

	public void clearHard(int version) {
		SwingUtilities.invokeLater(() -> model.clear(version, true));
	}

	public void clearSoft(int version) {
		SwingUtilities.invokeLater(() -> model.clear(version, false));
	}

	void addF(long[] values, int length) {
		SwingUtilities.invokeLater(() -> model.addF(values, length));
	}

	void addB(long[] values, int length) {
		SwingUtilities.invokeLater(() -> model.addB(values, length));
	}

	void setSourceSize(long sourceSize) {
		SwingUtilities.invokeLater(() -> model.setSourceSize(sourceSize));
	}

}
