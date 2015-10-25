package pl.rychu.jew.gui.search;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;



public class SearchDialog extends JDialog {

	private static final long serialVersionUID = 5308441657084566659L;

	private static final String ACTION_KEY_GLOB_ENTER = "jew.search.enter";
	private static final String ACTION_KEY_GLOB_ESC = "jew.search.esc";

	// --------------------------

	private JTextField textField;
	private JRadioButton downSearch;
	private JRadioButton searchPlainText;

	private boolean okPressed;

	// --------------------------

	public SearchDialog(JFrame jFrame) {
		super(jFrame, "Search", true);

		setSize(400, 150);

		Container cp = getContentPane();

		createComponents(cp);
		createActions((JComponent)cp);
		createKeyBindings((JComponent)cp);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	// --------------------------

	private void createComponents(Container cp) {
		cp.setLayout(new BorderLayout());

		textField = new JTextField(40);

		cp.add(textField, BorderLayout.NORTH);

		JPanel botPanel = new JPanel();
		botPanel.setLayout(new BorderLayout());

		JPanel optsPanel = new JPanel();
		optsPanel.setLayout(new BorderLayout());

		ButtonGroup bgSearchDir = new ButtonGroup();
		JPanel optsDirPanel = new JPanel();
		downSearch = new JRadioButton("down", true);
		bgSearchDir.add(downSearch);
		optsDirPanel.add(downSearch);
		JRadioButton upSearch = new JRadioButton("up");
		bgSearchDir.add(upSearch);
		optsDirPanel.add(upSearch);
		optsPanel.add(optsDirPanel, BorderLayout.WEST);

		ButtonGroup bgSearchKind = new ButtonGroup();
		JPanel optsSearchPanel = new JPanel();
		searchPlainText = new JRadioButton("text", true);
		bgSearchKind.add(searchPlainText);
		optsSearchPanel.add(searchPlainText);
		JRadioButton searchRegexp = new JRadioButton("regexp");
		bgSearchKind.add(searchRegexp);
		optsSearchPanel.add(searchRegexp);
		optsPanel.add(optsSearchPanel, BorderLayout.EAST);

		botPanel.add(optsPanel, BorderLayout.NORTH);

		JPanel butPanel = new JPanel();
		butPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		JButton buttonClose = new JButton("close");
		buttonClose.addActionListener(new DialogCloser(false));
		butPanel.add(buttonClose);

		JButton buttonSearch = new JButton("Search");
		buttonSearch.addActionListener(new DialogCloser(true));
		butPanel.add(buttonSearch);

		botPanel.add(butPanel, BorderLayout.SOUTH);

		cp.add(botPanel, BorderLayout.SOUTH);
	}

	private void createActions(JComponent jp) {
		final ActionMap actionMap = new ActionMap();
		final ActionMap oldActionMap = jp.getActionMap();
		actionMap.setParent(oldActionMap);
		jp.setActionMap(actionMap);

		actionMap.put(ACTION_KEY_GLOB_ENTER, new AbstractAction(ACTION_KEY_GLOB_ENTER) {
			private static final long serialVersionUID = -7854156520657704373L;
			@Override
			public void actionPerformed(ActionEvent e) {
				new DialogCloser(true).actionPerformed(e);
			}
		});

		actionMap.put(ACTION_KEY_GLOB_ESC, new AbstractAction(ACTION_KEY_GLOB_ESC) {
			private static final long serialVersionUID = -8974809846867740540L;
			@Override
			public void actionPerformed(ActionEvent e) {
				new DialogCloser(false).actionPerformed(e);
			}
		});

	}

	private void createKeyBindings(JComponent jp) {
		final InputMap inputMap = new InputMap();
		final InputMap oldInputMap = jp.getInputMap();
		inputMap.setParent(oldInputMap);
		jp.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);

		inputMap.put(KeyStroke.getKeyStroke("pressed ENTER"), ACTION_KEY_GLOB_ENTER);
		inputMap.put(KeyStroke.getKeyStroke("pressed ESCAPE"), ACTION_KEY_GLOB_ESC);
	}

	// --------------------------

	public void setFocusToText() {
		textField.requestFocus();
		textField.setSelectionStart(0);
		textField.setSelectionEnd(textField.getText().length());
	}

	// --------------------------

	public String getSearchText() {
		return textField.getText();
	}

	public boolean isOkPressed() {
		return okPressed;
	}

	public boolean isDownSearch() {
		return downSearch.isSelected();
	}

	public boolean isSearchPlaintext() {
		return searchPlainText.isSelected();
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
