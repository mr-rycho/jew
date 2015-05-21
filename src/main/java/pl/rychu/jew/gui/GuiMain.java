package pl.rychu.jew.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.LogAccess;
import pl.rychu.jew.LogAccessFile;
import pl.rychu.jew.LogLine;
import pl.rychu.jew.filter.LogLineFilterAll;
import pl.rychu.jew.filter.LogLineThreadFilter;



public class GuiMain {

	private static final Logger log = LoggerFactory.getLogger(GuiMain.class);


	public static void main(final String... args) throws InterruptedException {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final LogAccess logAccess
				 = LogAccessFile.create("/home/rycho/Pulpit/server.log");

				final JFrame mainFrame = new JFrame("jew");

				mainFrame.setLayout(new BorderLayout());

				final JPanel topPanel = new JPanel();
				topPanel.setLayout(new BorderLayout());
				mainFrame.add(topPanel, BorderLayout.NORTH);

				final InfoPanel infoPanel = new InfoPanel();
				topPanel.add(infoPanel, BorderLayout.CENTER);

				final AtomicBoolean testModel = new AtomicBoolean(false);

				final ListModelLog model = ListModelLog.create(logAccess);

				final LogViewPanel logViewPanel = LogViewPanel.create(model);

				logViewPanel.addListSelectionListener(infoPanel);
				model.addCyclicModelListener(infoPanel);
				model.addCyclicModelListener(logViewPanel);

				final JScrollPane scrollPane = new JScrollPane(logViewPanel);
				scrollPane.setPreferredSize(new Dimension(900, 600));
				mainFrame.add(scrollPane, BorderLayout.CENTER);

				JButton testButton = new JButton("switch models");
				topPanel.add(testButton, BorderLayout.WEST);
				testButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final boolean oldM = testModel.get();
						final boolean newM = !oldM;
						testModel.set(newM);
						if (!newM) {
							final int view = getView(logViewPanel);
							final long rootIndex = model.getRootIndex(view);
							log.debug("switching model");
							model.setFiltering(rootIndex, new LogLineFilterAll());
							log.debug("model switched");
						} else {
							final int view = getView(logViewPanel);
							log.debug("switching to filter with view = {}", view);
							String threadName = "EJB default - 2";
							long initIndex = 0L;
							final LogLine logLine = model.getIndexElementAdd(view);
							if (logLine != null) {
								threadName = logLine.getThreadName();
								initIndex = view;
							}
							if (threadName==null || threadName.isEmpty()) {
								return;
							}
							log.debug("switching model");
							model.setFiltering(initIndex, new LogLineThreadFilter(threadName));
							log.debug("model switched");
						}
					}
				});

				mainFrame.pack();
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainFrame.setTitle("Java log viEW");
				mainFrame.setVisible(true);
			}
		});
	}

	private static int getView(final LogViewPanel logViewPanel) {
		final int selectedIndex = logViewPanel.getSelectedIndex();
		if (selectedIndex >= 0) {
			return selectedIndex;
		}
		return logViewPanel.getFirstVisibleIndex();
	}

}
