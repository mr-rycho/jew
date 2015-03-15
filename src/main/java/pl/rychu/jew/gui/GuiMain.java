package pl.rychu.jew.gui;

import javax.swing.JFrame;

import pl.rychu.jew.LogFileAccess;



public class GuiMain {

	public static void main(final String... args) throws InterruptedException {
		final LogFileAccess logFileAccess
		 = new LogFileAccess("/home/rycho/Pulpit/server.log");

		Thread.sleep(1000L);

		final JFrame mainFrame = new JFrame("jew");

		final LogViewPanel logViewPanel = new LogViewPanel(logFileAccess);
		mainFrame.add(logViewPanel);

		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setTitle("Java log viEW");
		mainFrame.setVisible(true);
	}

}
