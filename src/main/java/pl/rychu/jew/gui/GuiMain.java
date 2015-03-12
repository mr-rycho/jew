package pl.rychu.jew.gui;

import javax.swing.JFrame;
import javax.swing.JLabel;



public class GuiMain {

	public static void main(final String... args) {
		final JFrame mainFrame = new JFrame("jew");
		final JLabel label = new JLabel("hello");
		mainFrame.getContentPane().add(label);

		mainFrame.pack();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}

}
