package pl.rychu.jew.gui.dlgs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class FilterRegexDialog extends JDialog {

	private static final long serialVersionUID = 919767162909843709L;

	private static final String ACTION_KEY_GLOB_ENTER = "jew.regex.enter";
	private static final String ACTION_KEY_GLOB_ESC = "jew.regex.esc";

	// --------------------------

	private JTextField textField;

	private Optional<Pattern> regexPatternOpt = Optional.empty();

	// --------------------------

	public FilterRegexDialog(JFrame jFrame) {
		super(jFrame, "Filter by regex", true);

		setSize(600, 150);

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

		botPanel.add(optsPanel, BorderLayout.NORTH);

		JPanel butPanel = new JPanel();
		butPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		JButton buttonClose = new JButton("close");
		buttonClose.addActionListener(new DialogCanceller());
		butPanel.add(buttonClose);

		JButton buttonSearch = new JButton("Search");
		buttonSearch.addActionListener(new DialogConfirmer());
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
			private static final long serialVersionUID = -6259443137845727197L;
			@Override
			public void actionPerformed(ActionEvent e) {
				new DialogConfirmer().actionPerformed(e);
			}
		});

		actionMap.put(ACTION_KEY_GLOB_ESC, new AbstractAction(ACTION_KEY_GLOB_ESC) {
			private static final long serialVersionUID = -1677594913030251407L;
			@Override
			public void actionPerformed(ActionEvent e) {
				new DialogCanceller().actionPerformed(e);
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
	}

	// --------------------------

	public Optional<Pattern> getRegexPatternOpt() {
		return regexPatternOpt;
	}

	public void turnOffRegex() {
		regexPatternOpt = Optional.empty();
	}

	// ========================

	private class DialogCanceller implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			regexPatternOpt = Optional.empty();
			setVisible(false);
		}
	}

	private class DialogConfirmer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			String text = textField.getText();
			try {
				Pattern pattern = Pattern.compile(text);
				regexPatternOpt = Optional.of(pattern);
				setVisible(false);
			} catch (PatternSyntaxException e) {
				// TODO okienko
				int minIndex = Math.max(0, e.getIndex());
				textField.setSelectionStart(minIndex);
				textField.setSelectionEnd(text.length());
				textField.requestFocus();
			}
		}
	}

}
