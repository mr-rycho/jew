package pl.rychu.jew.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;



public class InfoPanel extends JPanel {

	private static final long serialVersionUID = 5701049831143954538L;

	// ---------------

	private final JLabel lineCountLabel;

	// ---------------

	public InfoPanel() {
		super(true);

		lineCountLabel = new JLabel("#");

		this.add(lineCountLabel);
	}

	public void setLineCount(final int count) {
		lineCountLabel.setText(Integer.toString(count));
	}

}
