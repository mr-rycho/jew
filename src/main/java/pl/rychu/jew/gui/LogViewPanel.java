package pl.rychu.jew.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class LogViewPanel extends JPanel {

	private static final long serialVersionUID = -6731368974272464443L;

	// ---------

	public LogViewPanel() {
		super(true);

		setPreferredSize(new Dimension(500, 270));

		{
			LayoutManager layoutMan = getLayout();
			if (layoutMan instanceof FlowLayout) {
				FlowLayout flowLayout = (FlowLayout)layoutMan;
				flowLayout.setAlignment(FlowLayout.LEFT);
			}
		}

		for (int i=0; i<10; i++) {
			final JLabel label = new JLabel("line "+i);
			label.setPreferredSize(new Dimension(500, 15));
			add(label);
		}
	}

}
