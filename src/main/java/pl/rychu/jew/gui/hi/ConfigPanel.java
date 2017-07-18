package pl.rychu.jew.gui.hi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.rychu.jew.util.ColorUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;



public class ConfigPanel extends JPanel {

	private static final long serialVersionUID = 5612974465196310018L;

	private static final Logger log = LoggerFactory.getLogger(ConfigPanel.class);


	private final JTextField regexEditField;
	private final JTextField colorBackField;
	private final JTextField colorForeField;

	private Collection<HiEntryChangeListener> listeners
	 = new CopyOnWriteArrayList<>();

	private boolean textChangeEventsCanGoOutside = true;

	// -----------------

	ConfigPanel() {
		final JPanel regexEditPanel = new JPanel();
		regexEditPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		regexEditField = new JTextField(20);
		regexEditPanel.add(regexEditField);

		final JPanel colorpickPanel = new JPanel();
		colorpickPanel.setMinimumSize(new Dimension(300, 40));
		colorpickPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		final JButton pickBackButton = new JButton("back");
		colorpickPanel.add(pickBackButton);
		colorBackField = new JTextField(10);
		colorpickPanel.add(colorBackField);
		final JButton pickForeButton = new JButton("fore");
		colorpickPanel.add(pickForeButton);
		colorForeField = new JTextField(10);
		colorpickPanel.add(colorForeField);

		pickBackButton.addActionListener(new ColorButton("background", colorBackField, 0xffffff));
		pickForeButton.addActionListener(new ColorButton("foreground", colorForeField, 0x000000));

		TextChangeListener textChangeListener = new TextChangeListener();
		regexEditField.getDocument().addDocumentListener(textChangeListener);
		colorBackField.getDocument().addDocumentListener(textChangeListener);
		colorForeField.getDocument().addDocumentListener(textChangeListener);

		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(300, 80));
		add(regexEditPanel, BorderLayout.NORTH);
		add(colorpickPanel, BorderLayout.SOUTH);
	}

	public void clear() {
		textChangeEventsCanGoOutside = false;
		try {
			regexEditField.setText("");
			colorBackField.setText("");
			colorForeField.setText("");
			notifyHiEntryChangeFromOutside();
		} finally {
			textChangeEventsCanGoOutside = true;
		}
	}

	public void put(HiConfigEntry hiConfigEntry) {
		textChangeEventsCanGoOutside = false;
		try {
			regexEditField.setText(hiConfigEntry.getRegexp());
			colorBackField.setText(ColorUtil.toCssColor(hiConfigEntry.getColorB()));
			colorForeField.setText(ColorUtil.toCssColor(hiConfigEntry.getColorF()));
		} finally {
			textChangeEventsCanGoOutside = true;
		}
	}

	public HiConfigEntry get() {
		String regex = regexEditField.getText();
		String colorBStr = colorBackField.getText();
		String colorFStr = colorForeField.getText();
		int colorB = ColorUtil.getColor(colorBStr, 0xffffff);
		int colorF = ColorUtil.getColor(colorFStr, 0x000000);
		return new HiConfigEntry(regex, colorB, colorF);
	}

	// ---

	void addHiEntryChangeListener(HiEntryChangeListener lsn) {
		listeners.add(lsn);
	}

	public void removeHiEntryChangeListener(HiEntryChangeListener lsn) {
		listeners.remove(lsn);
	}

	private void conditionallyNotifyHiEntryChangeFromInside() {
		if (textChangeEventsCanGoOutside) {
			notifyHiEntryChangeFromInside();
		}
	}

	private void notifyHiEntryChangeFromInside() {
		notifyHiEntryChange(true);
	}

	private void notifyHiEntryChangeFromOutside() {
		notifyHiEntryChange(false);
	}

	private void notifyHiEntryChange(boolean fromInside) {
		for (HiEntryChangeListener lsn: listeners) {
			try {
				lsn.hiEntryChanged(fromInside);
			} catch (Exception e) {
				log.error("error calling listener", e);
			}
		}
	}

	// ==========

	private class TextChangeListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			conditionallyNotifyHiEntryChangeFromInside();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			conditionallyNotifyHiEntryChangeFromInside();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			conditionallyNotifyHiEntryChangeFromInside();
		}
	}

	// ==========

	private class ColorButton implements ActionListener {
		private String title;
		private JTextField textField;
		private int defaultColor;

		private ColorButton(String title, JTextField textField, int defaultColor) {
			this.title = title;
			this.textField = textField;
			this.defaultColor = defaultColor;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Color initColor = new Color(ColorUtil.getColor(textField.getText(), defaultColor));
			Color newColor = JColorChooser.showDialog(ConfigPanel.this, title, initColor);
			if (newColor != null) {
				textField.setText(ColorUtil.toCssColor(newColor.getRGB()));
			}
		}
	}

}