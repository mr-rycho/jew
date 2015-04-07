package pl.rychu.jew.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import pl.rychu.jew.LogFileAccess;



public class GuiMain {

	public static void main(final String... args) throws InterruptedException {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final LogFileAccess logFileAccess
				 = LogFileAccess.create("/home/rycho/Pulpit/server.log");

				final JFrame mainFrame = new JFrame("jew");

				mainFrame.setLayout(new BorderLayout());

				final InfoPanel infoPanel = new InfoPanel();
				mainFrame.add(infoPanel, BorderLayout.NORTH);

				final ListModelLog model = ListModelLog.create(logFileAccess);

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
