package pl.rychu.jew.gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import pl.rychu.jew.LogFileAccess;



public class GuiMain {

	public static void main(final String... args) throws InterruptedException {
		final LogFileAccess logFileAccess
		 = LogFileAccess.create("/home/rycho/Pulpit/server.log");

		Thread.sleep(1000L);

		final JFrame mainFrame = new JFrame("jew");

		final LogViewPanel logViewPanel = new LogViewPanel(logFileAccess);

		final JScrollPane scrollPane = new JScrollPane(logViewPanel);
		scrollPane.setPreferredSize(new Dimension(600, 600));
		mainFrame.add(scrollPane);

		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setTitle("Java log viEW");
		mainFrame.setVisible(true);
	}

}
