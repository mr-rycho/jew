package pl.rychu.jew.gui;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;



public class InfoPanel extends JPanel implements CyclicModelListener
 , ListSelectionListener {

	private static final long serialVersionUID = 5701049831143954538L;

	// ---------------

	private final JLabel currentLine;
	private final JLabel lineCountLabel;
	private final JLabel rootIndex;
	private final JLabel rootSize;

	// ---------------

	public InfoPanel() {
		super(true);

		currentLine = new JLabel("#");
		this.add(currentLine);

		lineCountLabel = new JLabel("#");
		this.add(lineCountLabel);

		rootIndex = new JLabel("#");
		this.add(rootIndex);

		rootSize = new JLabel("#");
		this.add(rootSize);
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
	public void sourceChanged(long totalSourceLines) {
		setRootSize(totalSourceLines);
	}

	@Override
	public void valueChanged(final ListSelectionEvent e) {
		final Object sourceObj = e.getSource();
		if (sourceObj instanceof LogViewPanel) {
			final LogViewPanel panel = (LogViewPanel)sourceObj;
			final int firstIndex = panel.getSelectedIndex();
			setCurrentLine(firstIndex);
			setRootIndex(getRootIndex(panel, firstIndex));
		}
	}

	// --------------

	private long getRootIndex(final JList<?> list, final int firstIndex) {
		final ListModel<?> modelRaw = list.getModel();
		if (modelRaw instanceof ListModelLog) {
			final ListModelLog model = (ListModelLog)modelRaw;
			final int size = model.getSize();
			if (firstIndex>=0 && firstIndex<size) {
			return model.getRootIndex(firstIndex);
			}
		}
		return -1;
	}

	// --------------

	private void setCurrentLine(final int number) {
		final String numStr = number < 0 ? "-" : Integer.toString(number+1);
		currentLine.setText(numStr);
	}

	private void setLineCount(final int count) {
		lineCountLabel.setText(Integer.toString(count));
	}

	private void setRootIndex(final long number) {
		final String numStr = number < 0 ? "-" : Long.toString(number+1);
		rootIndex.setText(numStr);
	}

	private void setRootSize(final long number) {
		final String numStr = number < 0 ? "-" : Long.toString(number);
		rootSize.setText(numStr);
	}

}
