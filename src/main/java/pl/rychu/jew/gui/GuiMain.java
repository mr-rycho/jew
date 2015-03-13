package pl.rychu.jew.gui;

import javax.swing.JFrame;



public class GuiMain {

	public static void main(final String... args) {
		final JFrame mainFrame = new JFrame("jew");

		final LogViewPanel logViewPanel = new LogViewPanel();
		mainFrame.add(logViewPanel);

		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setTitle("Java log viEW");
		mainFrame.setVisible(true);
	}

}
