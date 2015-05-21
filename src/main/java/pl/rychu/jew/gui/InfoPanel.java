package pl.rychu.jew.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;



public class InfoPanel extends JPanel implements CyclicModelListener
 , ListSelectionListener {

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

	// --------------

	@Override
	public void linesAddedStart(final int numberOfLinesAdded, final long total) {
		setLineCount((int)total);
	}

	@Override
	public void linesAddedEnd(final int numberOfLinesAdded, final long total) {
		setLineCount((int)total);
	}

	@Override
	public void listReset() {
		setLineCount(0);
	}

	@Override
	public void valueChanged(final ListSelectionEvent e) {
		final Object sourceObj = e.getSource();
		if (sourceObj instanceof LogViewPanel) {
			final LogViewPanel panel = (LogViewPanel)sourceObj;
			final int firstIndex = panel.getSelectedIndex();
			setCurrentLine(firstIndex);
		}
	}

	// --------------

	private void setCurrentLine(final int number) {
		final String numStr = number < 0 ? "-" : Integer.toString(number+1);
		currentLine.setText(numStr);
	}

	private void setLineCount(final int count) {
		lineCountLabel.setText(Integer.toString(count));
	}

}
