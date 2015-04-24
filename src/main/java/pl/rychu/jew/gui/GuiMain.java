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

import pl.rychu.jew.LogAccess;
import pl.rychu.jew.LogAccessFilter;
import pl.rychu.jew.LogFileAccess;
import pl.rychu.jew.filter.LogLineThreadFilter;



public class GuiMain {

	public static void main(final String... args) throws InterruptedException {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final LogAccess logFileAccess
				 = LogFileAccess.create("/home/rycho/Pulpit/server.log");
				final LogAccess logAccessFilter = LogAccessFilter.create(logFileAccess
				 , new LogLineThreadFilter("EJB default - 2"), 0);

				final JFrame mainFrame = new JFrame("jew");

				mainFrame.setLayout(new BorderLayout());

				final JPanel topPanel = new JPanel();
				topPanel.setLayout(new BorderLayout());
				mainFrame.add(topPanel, BorderLayout.NORTH);

				final InfoPanel infoPanel = new InfoPanel();
				topPanel.add(infoPanel, BorderLayout.CENTER);

				final AtomicBoolean testModel = new AtomicBoolean();

				final ListModelLog model = ListModelLog.create(logAccessFilter);

				JButton testButton = new JButton("switch models");
				topPanel.add(testButton, BorderLayout.WEST);
				testButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final boolean oldM = testModel.get();
						final boolean newM = !oldM;
						testModel.set(newM);
						if (newM) {
							final LogAccess oldLogAccess = model.getLogAccess();
							model.setLogAccess(logFileAccess);
							((LogAccessFilter)oldLogAccess).dispose();
						} else {
							final LogAccess logAccessFilter = LogAccessFilter.create(logFileAccess
							 , new LogLineThreadFilter("EJB default - 2"), 0);
							model.setLogAccess(logAccessFilter);
						}
					}
				});

				final LogViewPanel logViewPanel = LogViewPanel.create(model);

				logViewPanel.addListSelectionListener(infoPanel);
				model.addCyclicModelListener(infoPanel);
				model.addCyclicModelListener(logViewPanel);

				final JScrollPane scrollPane = new JScrollPane(logViewPanel);
				scrollPane.setPreferredSize(new Dimension(700, 600));
				mainFrame.add(scrollPane, BorderLayout.CENTER);

				mainFrame.pack();
				mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainFrame.setTitle("Java log viEW");
				mainFrame.setVisible(true);
			}
		});
	}

}
