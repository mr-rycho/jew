package pl.rychu.jew.gui.search;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;



public class SearchDialog extends JDialog {

	private static final long serialVersionUID = 5308441657084566659L;


	private JTextField textField;

	private boolean okPressed;


	public SearchDialog(JFrame jFrame) {
		super(jFrame, "Search", true);

		setSize(400, 200);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		textField = new JTextField(40);

		cp.add(textField, BorderLayout.NORTH);

		JPanel botPanel = new JPanel();
		botPanel.setLayout(new BorderLayout());

		JPanel optsPanel = new JPanel();
		optsPanel.setLayout(new BorderLayout());

		JList<String> list1 = new JList<>(new String[]{"down", "up"});
		optsPanel.add(list1, BorderLayout.WEST);

		JList<String> list2 = new JList<>(new String[]{"text", "regexp"});
		optsPanel.add(list2, BorderLayout.EAST);

		botPanel.add(optsPanel, BorderLayout.NORTH);

		JPanel butPanel = new JPanel();
		butPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		JButton buttonSearch = new JButton("Search");
		buttonSearch.addActionListener(new DialogCloser(true));
		butPanel.add(buttonSearch);

		JButton buttonClose = new JButton("close");
		buttonClose.addActionListener(new DialogCloser(false));
		butPanel.add(buttonClose);

		botPanel.add(butPanel, BorderLayout.SOUTH);

		cp.add(botPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	public String getSearchText() {
		return textField.getText();
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	// ========================

	private class DialogCloser implements ActionListener {
		private final boolean opResult;

		private DialogCloser(boolean opResult) {
			this.opResult = opResult;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			okPressed = opResult;
			setVisible(false);
		}
	}

}
