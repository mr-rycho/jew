package pl.rychu.jew.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import pl.rychu.jew.gui.hi.HiConfigProviderPer;
import pl.rychu.jew.logaccess.LogAccess;
import pl.rychu.jew.logaccess.LogAccessFile;



public class GuiMain {

	public static void runGuiAsynchronously(String filename, boolean isWindows) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final JFrame mainFrame = createFrame(filename, isWindows);
				mainFrame.setVisible(true);
			}
		});
	}

	private static JFrame createFrame(String filename, boolean isWindows) {
		final LogAccess logAccess = LogAccessFile.create(filename, isWindows);

		final JFrame mainFrame = new JFrame("jew");

		mainFrame.setLayout(new BorderLayout());

		final ListModelLog model = ListModelLog.create(logAccess);

		final LogViewPanel logViewPanel = LogViewPanel.create(model);
		logViewPanel.setHiConfigProvider(new HiConfigProviderPer());

		final JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		mainFrame.add(topPanel, BorderLayout.NORTH);

		StatusPanel statusPanel = StatusPanel.create();
		mainFrame.add(statusPanel, BorderLayout.SOUTH);

		final InfoPanel infoPanel = InfoPanel.create(logViewPanel);
		topPanel.add(infoPanel, BorderLayout.CENTER);

		logViewPanel.setMessageConsumer(statusPanel);
		logViewPanel.addListSelectionListener(infoPanel);
		model.addCyclicModelListener(infoPanel);
		model.addPanelModelChangeListener(infoPanel);
		model.addCyclicModelListener(logViewPanel);
		model.addCyclicModelListener(new TitleHandler(mainFrame, filename));

		final JScrollPane scrollPane = new JScrollPane(logViewPanel);
		scrollPane.setPreferredSize(new Dimension(900, 600));
		scrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.fastWheelScrolling", true);
		mainFrame.add(scrollPane, BorderLayout.CENTER);

		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setTitle("Java log viEW");

		return mainFrame;
	}

	// ==============

	private static class TitleHandler implements CyclicModelListener {

		private final JFrame mainFrame;
		private final String filename;

		private TitleHandler(JFrame mainFrame, String filename) {
			this.mainFrame = mainFrame;
			this.filename = Math.abs(0)==0 ? filename : getFilename(filename);
		}

		@Override
		public void linesAddedStart(int numberOfLinesAdded, long totalLines) {}

		@Override
		public void linesAddedEnd(int numberOfLinesAdded, long totalLines) {}

		@Override
		public void listReset(boolean sourceReset) {}

		@Override
		public void sourceChanged(long number) {
			final String numStr = number < 0 ? "-" : Long.toString(number);
			mainFrame.setTitle(numStr+" - "+filename);
		}

		private String getFilename(String fullPath) {
			int index = Math.max(fullPath.lastIndexOf('/'), fullPath.lastIndexOf('\\'));
			return index>=0 ? fullPath.substring(index+1) : fullPath;
		}
	}

}
