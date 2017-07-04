package pl.rychu.jew.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pl.rychu.jew.filter.LogLineFilterChain;
import pl.rychu.jew.util.StringUtil;



public class InfoPanel extends JPanel implements CyclicModelListener
 , ListSelectionListener, PanelModelChangeListener {

	private static final long serialVersionUID = 5701049831143954538L;

	// ---------------

	private final LogViewPanel logViewPanel;

	private final JLabel currentLine;
	private final JLabel lineCountLabel;
	private final JLabel rootIndex;
	private final JLabel rootSize;
	private final JLabel panelProps;
	private final JLabel modelProps;
	private final JLabel filterThreadsLabel;

	// ---------------

	private InfoPanel(final LogViewPanel logViewPanel) {
		super(); //(true);

		this.logViewPanel = logViewPanel;

		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		add(topPanel, BorderLayout.NORTH);
		topPanel.setLayout(new BorderLayout());
		JPanel topLeftPanel = new JPanel();
		topLeftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(topLeftPanel, BorderLayout.WEST);
		JPanel topRightPanel = new JPanel();
		topRightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		topPanel.add(topRightPanel, BorderLayout.EAST);

		JPanel botPanel = new JPanel();
		add(botPanel, BorderLayout.SOUTH);
		botPanel.setLayout(new BorderLayout());
		JPanel botLeftPanel = new JPanel();
		botLeftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		botPanel.add(botLeftPanel, BorderLayout.WEST);
		JPanel botRightPanel = new JPanel();
		botRightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		botPanel.add(botRightPanel, BorderLayout.EAST);

		currentLine = new JLabel("#");
		topLeftPanel.add(currentLine);

		lineCountLabel = new JLabel("#");
		topLeftPanel.add(lineCountLabel);

		rootIndex = new JLabel("#");
		topLeftPanel.add(rootIndex);

		rootSize = new JLabel("#");
		topLeftPanel.add(rootSize);

		panelProps = new JLabel("--");
		topRightPanel.add(panelProps);

		modelProps = new JLabel("--");
		botLeftPanel.add(modelProps);

		filterThreadsLabel = new JLabel("");
		botRightPanel.add(filterThreadsLabel);
	}

	public static InfoPanel create(final LogViewPanel logViewPanel) {
		final InfoPanel result = new InfoPanel(logViewPanel);

		logViewPanel.addPanelModelChangeListener(result);

		return result;
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
	public void listReset(final boolean sourceReset) {
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

	@Override
	public void panelChanged() {
		final boolean tail = logViewPanel.isTail();
		StringBuilder sb = new StringBuilder();
		sb.append(tail ? "TAIL" : "tail-");
		setPanelProps(sb.toString());
		String[] thr = new ArrayList<>(logViewPanel.getFilterThreads()).toArray(new String[0]);
		if (thr.length == 0) {
			setThreadList("");
		} else {
			Arrays.sort(thr);
			String str = StringUtil.join(thr, "{", "}", "\"", "\"", " OR ");
			setThreadList(str);
		}
	}

	@Override
	public void modelChanged() {
		ListModelLog model = (ListModelLog)logViewPanel.getModel();
		LogLineFilterChain filterChain = model.getFilterChain();
		setModelProps(filterChain.toString());
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

	private void setPanelProps(final String propsStr) {
		panelProps.setText(propsStr);
	}

	private void setModelProps(final String propsStr) {
		modelProps.setText(propsStr);
	}

	private void setThreadList(String threadsStr) {
		filterThreadsLabel.setText(threadsStr);
	}

}
