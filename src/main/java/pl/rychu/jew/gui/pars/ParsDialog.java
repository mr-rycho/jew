package pl.rychu.jew.gui.pars;

import javax.swing.*;

/**
 * Created on 21.07.2017.
 */
public class ParsDialog extends JDialog {

	public ParsDialog(JFrame fr, ParsConfig parsConfig, ParsConfigChangeListener lsn) {
		super(fr, "Parse Dialog", true);
		setSize(450, 400);
		setVisible(true);
	}

}
