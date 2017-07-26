package pl.rychu.jew.gui.pars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
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

	private Collection<ParsEntryChangeListener> listeners = new CopyOnWriteArrayList<>();

	private boolean textChangeEventsCanGoOutside = true;

	// ----------

	public ParsEditPanel() {
		setLayout(new BorderLayout(5, 5));

		nameField = new JTextField(20);

		regexArea = new JTextArea(20, 5);
		regexArea.setBorder(nameField.getBorder());

		add(nameField, BorderLayout.NORTH);
		add(regexArea, BorderLayout.CENTER);
	}

	// ----------

	public void clear() {
		textChangeEventsCanGoOutside = false;
		try {
			nameField.setText("");
			regexArea.setText("");
			// TODO fields

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
		} finally {
			textChangeEventsCanGoOutside = true;
		}
	}

	public ParsConfigEntry get() {
		return new ParsConfigEntry(nameField.getText(), regexArea.getText().replace("\n", ""), 1, 2,
		 3, 4, 5);
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

}
