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

	private static final String ACTION_KEY_APPLY_FILTER = "jew.regex.apply";
	private static final String ACTION_KEY_CANCEL = "jew.regex.cancel";
	private static final String ACTION_KEY_TURN_OFF = "jew.regex.turnoff";

	// --------------------------

	private JTextField textField;

	private Optional<Pattern> regexPatternOpt = Optional.empty();
	private boolean wasCancelled;
	private JCheckBox chInvert;
	private JCheckBox chCaseSens;

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
		optsPanel.setLayout(new FlowLayout());// new BorderLayout());

		chInvert = new JCheckBox("invert");
		optsPanel.add(chInvert);

		chCaseSens = new JCheckBox("case sensitive");
		optsPanel.add(chCaseSens);

		botPanel.add(optsPanel, BorderLayout.NORTH);

		JPanel butPanel = new JPanel();
		butPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		JButton buttonCancel = new JButton("cancel");
		buttonCancel.addActionListener(new DialogCanceller());
		butPanel.add(buttonCancel);

		JButton buttonTurnOff = new JButton("turn off");
		buttonTurnOff.addActionListener(new FilterOffturner());
		butPanel.add(buttonTurnOff);

		JButton buttonFilter = new JButton("Filter");
		buttonFilter.addActionListener(new DialogConfirmer());
		butPanel.add(buttonFilter);

		botPanel.add(butPanel, BorderLayout.SOUTH);

		cp.add(botPanel, BorderLayout.SOUTH);
	}

	private void createActions(JComponent jp) {
		final ActionMap actionMap = new ActionMap();
		final ActionMap oldActionMap = jp.getActionMap();
		actionMap.setParent(oldActionMap);
		jp.setActionMap(actionMap);

		actionMap.put(ACTION_KEY_APPLY_FILTER, new AbstractAction(ACTION_KEY_APPLY_FILTER) {
			private static final long serialVersionUID = -6259443137845727197L;
			@Override
			public void actionPerformed(ActionEvent e) {
				new DialogConfirmer().actionPerformed(e);
			}
		});

		actionMap.put(ACTION_KEY_CANCEL, new AbstractAction(ACTION_KEY_CANCEL) {
			private static final long serialVersionUID = -1677594913030251407L;
			@Override
			public void actionPerformed(ActionEvent e) {
				new DialogCanceller().actionPerformed(e);
			}
		});

		actionMap.put(ACTION_KEY_TURN_OFF, new AbstractAction(ACTION_KEY_TURN_OFF) {
			private static final long serialVersionUID = -7259707891823295350L;
			@Override
			public void actionPerformed(ActionEvent e) {
				new FilterOffturner().actionPerformed(e);
			}
		});

	}

	private void createKeyBindings(JComponent jp) {
		final InputMap inputMap = new InputMap();
		final InputMap oldInputMap = jp.getInputMap();
		inputMap.setParent(oldInputMap);
		jp.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);

		inputMap.put(KeyStroke.getKeyStroke("pressed ENTER"), ACTION_KEY_APPLY_FILTER);
		inputMap.put(KeyStroke.getKeyStroke("pressed ESCAPE"), ACTION_KEY_CANCEL);
	}

	// --------------------------

	public void setFocusToText() {
		textField.requestFocus();
	}

	// --------------------------

	public Optional<Pattern> getRegexPatternOpt() {
		return regexPatternOpt;
	}

	public boolean isWasCancelled() {
		return wasCancelled;
	}

	public boolean isInverted() {
		return chInvert.isSelected();
	}

	// ========================

	private class DialogCanceller implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			wasCancelled = true;
			setVisible(false);
		}
	}

	private class FilterOffturner implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			regexPatternOpt = Optional.empty();
			wasCancelled = false;
			setVisible(false);
		}
	}

	private class DialogConfirmer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			String text = textField.getText();
			try {
				int caseSens = chCaseSens.isSelected() ? 0 : Pattern.CASE_INSENSITIVE;
				Pattern pattern = Pattern.compile(text, caseSens);
				regexPatternOpt = Optional.of(pattern);
				wasCancelled = false;
				setVisible(false);
			} catch (PatternSyntaxException e) {
				String message = "Error in regular expression:\n"+e.getClass().getName()+": "+e.getMessage();
				JLabel label = new JLabel(message);
				label.setForeground(Color.RED);
				JTextArea jTextArea = new JTextArea(message);
				jTextArea.setEditable(false);
				jTextArea.setOpaque(false);
				jTextArea.setFont(new Font("monospaced", Font.PLAIN, 12));
				JOptionPane.showMessageDialog(FilterRegexDialog.this, jTextArea);
				int minIndex = Math.max(0, e.getIndex());
				textField.setSelectionStart(minIndex);
				textField.setSelectionEnd(text.length());
				textField.requestFocus();
			}
		}
	}

}
