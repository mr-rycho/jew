package pl.rychu.jew.gui.pars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created on 24.07.2017.
 */
public class ParsEditPanel extends JPanel {

	private static final long serialVersionUID = -5628912256423788435L;
	private static final Logger log = LoggerFactory.getLogger(ParsEditPanel.class);

	private final JTextField nameField;
	private final JTextArea regexArea;
	private final JTextField groupTimeField;
	private final JTextField groupLevelField;
	private final JTextField groupClassField;
	private final JTextField groupThreadField;
	private final JTextField groupMessageField;

	private Collection<ParsEntryChangeListener> listeners = new CopyOnWriteArrayList<>();

	private boolean textChangeEventsCanGoOutside = true;

	// ----------

	public ParsEditPanel() {
		setLayout(new BorderLayout(5, 5));

		nameField = new JTextField(20);

		regexArea = new JTextArea(20, 5);
		regexArea.setBorder(nameField.getBorder());
		Font font = regexArea.getFont();
		regexArea.setFont(new Font(Font.MONOSPACED, font.getStyle() | Font.BOLD, font.getSize()));

		JPanel groupsPanel = new JPanel();
		groupsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		groupTimeField = createAndAddTextFieldWithLabel("time", groupsPanel);
		groupLevelField = createAndAddTextFieldWithLabel("level", groupsPanel);
		groupClassField = createAndAddTextFieldWithLabel("class", groupsPanel);
		groupThreadField = createAndAddTextFieldWithLabel("thread", groupsPanel);
		groupMessageField = createAndAddTextFieldWithLabel("message", groupsPanel);

		add(nameField, BorderLayout.NORTH);
		add(regexArea, BorderLayout.CENTER);
		add(groupsPanel, BorderLayout.SOUTH);

		TextChangeListener textChangeListener = new TextChangeListener();
		nameField.getDocument().addDocumentListener(textChangeListener);
		regexArea.getDocument().addDocumentListener(textChangeListener);
		groupTimeField.getDocument().addDocumentListener(textChangeListener);
		groupLevelField.getDocument().addDocumentListener(textChangeListener);
		groupClassField.getDocument().addDocumentListener(textChangeListener);
		groupThreadField.getDocument().addDocumentListener(textChangeListener);
		groupMessageField.getDocument().addDocumentListener(textChangeListener);
	}

	private static JTextField createAndAddTextFieldWithLabel(String labelStr, JPanel targetPanel) {
		JTextField textField = new JTextField(5);
		JPanel panel = new JPanel();
		JLabel label = new JLabel(labelStr);
		label.setLabelFor(textField);
		panel.add(label);
		panel.add(textField);
		targetPanel.add(panel);
		return textField;
	}

	// ----------

	public void clear() {
		textChangeEventsCanGoOutside = false;
		try {
			nameField.setText("");
			regexArea.setText("");
			groupTimeField.setText("");
			groupLevelField.setText("");
			groupClassField.setText("");
			groupThreadField.setText("");
			groupMessageField.setText("");

			notifyParsEntryChangeFromOutside();
		} finally {
			textChangeEventsCanGoOutside = true;
		}
	}

	public void put(ParsConfigEntry parsConfigEntry) {
		textChangeEventsCanGoOutside = false;
		try {
			nameField.setText(parsConfigEntry.getName());
			regexArea.setText(parsConfigEntry.getPattern());
			groupTimeField.setText("" + parsConfigEntry.getGroupTime());
			groupLevelField.setText("" + parsConfigEntry.getGroupLevel());
			groupClassField.setText("" + parsConfigEntry.getGroupClass());
			groupThreadField.setText("" + parsConfigEntry.getGroupThread());
			groupMessageField.setText("" + parsConfigEntry.getGroupMessage());
		} finally {
			textChangeEventsCanGoOutside = true;
		}
	}

	public ParsConfigEntry get() {
		String name = nameField.getText();
		String regex = regexArea.getText();
		int groupTime = parseInt(groupTimeField);
		int groupLevel = parseInt(groupLevelField);
		int groupClass = parseInt(groupClassField);
		int groupThread = parseInt(groupThreadField);
		int groupMessage = parseInt(groupMessageField);
		return new ParsConfigEntry(name, regex, groupTime, groupLevel, groupClass, groupThread,
		 groupMessage);
	}

	private static int parseInt(JTextField textField) {
		String text = textField.getText().trim();
		try {
			return Integer.parseInt(text, 10);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	// ----------

	void addParsEntryChangeListener(ParsEntryChangeListener lsn) {
		listeners.add(lsn);
	}

	private void conditionallyNotifyParsEntryChangeFromInside() {
		if (textChangeEventsCanGoOutside) {
			notifyParsEntryChangeFromInside();
		}
	}

	private void notifyParsEntryChangeFromInside() {
		notifyParsEntryChange(true);
	}

	private void notifyParsEntryChangeFromOutside() {
		notifyParsEntryChange(false);
	}

	private void notifyParsEntryChange(boolean fromInside) {
		for (ParsEntryChangeListener lsn : listeners) {
			try {
				lsn.parsEntryChanged(fromInside);
			} catch (Exception e) {
				log.error("error calling listener", e);
			}
		}
	}

	// ==========

	private class TextChangeListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			conditionallyNotifyParsEntryChangeFromInside();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			conditionallyNotifyParsEntryChangeFromInside();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			conditionallyNotifyParsEntryChangeFromInside();
		}
	}

}
