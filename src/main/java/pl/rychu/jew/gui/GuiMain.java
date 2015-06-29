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

		final JScrollPane scrollPane = new JScrollPane(logViewPanel);
		scrollPane.setPreferredSize(new Dimension(900, 600));
		scrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.fastWheelScrolling", true);
		mainFrame.add(scrollPane, BorderLayout.CENTER);

		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setTitle("Java log viEW");

		return mainFrame;
	}

}
