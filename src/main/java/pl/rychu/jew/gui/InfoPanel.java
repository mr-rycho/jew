package pl.rychu.jew.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;



public class InfoPanel extends JPanel implements CyclicModelListener {

	private static final long serialVersionUID = 5701049831143954538L;

	// ---------------

	private final JLabel currentLine;
	private final JLabel lineCountLabel;

	// ---------------

	public InfoPanel() {
		super(true);

		currentLine = new JLabel("#");
		this.add(currentLine);

		lineCountLabel = new JLabel("#");
		this.add(lineCountLabel);
	}

	@Override
	public void linesAdded(int newSize) {
		setLineCount(newSize);
	}

	@Override
	public void listReset() {
		setLineCount(0);
	}

	public void setCurrentLine(final int number) {
		final String numStr = number < 0 ? "-" : Integer.toString(number+1);
		currentLine.setText(numStr);
	}

	private void setLineCount(final int count) {
		lineCountLabel.setText(Integer.toString(count));
	}

}
