package pl.rychu.jew.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;


public class HelpDialog extends JDialog {

	private static final long serialVersionUID = 7725762198704917031L;

	private static final String ACTION_KEY_GLOB_CLOSE = "jew.help.close";

	public HelpDialog(JFrame jFrame) {
		super(jFrame, "Help", true);

		setLayout(new BorderLayout());

		setSize(500, 400);

		Container cp = getContentPane();

		createActions((JComponent)cp);
		createComponents((JComponent)cp);
		createKeyBindings((JComponent)cp);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		setVisible(true);
	}

	private void createComponents(JComponent cp) {
		ActionMap actionMap = cp.getActionMap();
		JPanel centerPanel = new JPanel(new BorderLayout());
		JPanel botPanel = new JPanel(new BorderLayout());
		cp.add(centerPanel, BorderLayout.CENTER);
		cp.add(botPanel, BorderLayout.SOUTH);

		JTextArea helpText = new JTextArea();
		helpText.setEditable(false);
		helpText.setText(getHelpText());
		centerPanel.add(helpText);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		botPanel.add(buttonPanel, BorderLayout.CENTER);
		JButton closeButton = new JButton("close");
		buttonPanel.add(closeButton);
		closeButton.setAction(actionMap.get(ACTION_KEY_GLOB_CLOSE));
		closeButton.setText("close");
	}

	private void createActions(JComponent jp) {
		final ActionMap actionMap = new ActionMap();
		final ActionMap oldActionMap = jp.getActionMap();
		actionMap.setParent(oldActionMap);
		jp.setActionMap(actionMap);

		actionMap.put(ACTION_KEY_GLOB_CLOSE, new AbstractAction(ACTION_KEY_GLOB_CLOSE) {
			private static final long serialVersionUID = -7854156520657704373L;
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}

	private void createKeyBindings(JComponent jp) {
		final InputMap inputMap = new InputMap();
		final InputMap oldInputMap = jp.getInputMap();
		inputMap.setParent(oldInputMap);
		jp.setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);

		inputMap.put(KeyStroke.getKeyStroke("pressed ESCAPE"), ACTION_KEY_GLOB_CLOSE);
	}

	// ---------------

	private static String getHelpText() {
		StringBuilder sb = new StringBuilder();

		for (String str: getHelpLines()) {
			sb.append(str);
			sb.append("\n");
		}

		return sb.toString();
	}

	private static String[] getHelpLines() {
		return new String[] {
		 "` - toggle tail"
		 , "shift+h - open highlighting dialog"
		 , "t - toggle thread filter"
		 , "shift+s - toggle stack collapse"
		 , "ctrl+f - open search dialog"
		 , "f3 - search again"
		 , "[ - turn on filter from current line"
		 , "{ - turn off filter from current line"
		 , "] - turn on filter to current line"
		 , "} - turn off filter to current line"
		 , "shift+c - toggle classname collapse"
		 , "ctrl+,/. - navigate through exception causes"
		 , ""
		 , "wheel+ctrl - speed x 10"
		 , "wheel+ctrl+shift - speed x 100"
		};
	}

}
