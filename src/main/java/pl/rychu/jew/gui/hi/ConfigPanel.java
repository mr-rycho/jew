package pl.rychu.jew.gui.hi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import pl.rychu.jew.util.ColorUtil;



public class ConfigPanel extends JPanel {
	private static final long serialVersionUID = 5612974465196310018L;

	private final JTextField regexEditField;
	private final JTextField colorBackField;
	private final JTextField colorForeField;

	public ConfigPanel() {
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

		pickBackButton.addActionListener(new ColorButton("background", colorBackField));
		pickForeButton.addActionListener(new ColorButton("foreground", colorForeField));

		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(300, 80));
		add(regexEditPanel, BorderLayout.NORTH);
		add(colorpickPanel, BorderLayout.SOUTH);
	}

	public void clear() {
		regexEditField.setText("");
		colorBackField.setText("");
		colorForeField.setText("");
	}

	public void put(HiConfigEntry hiConfigEntry) {
		regexEditField.setText(hiConfigEntry.getRegexp());
		colorBackField.setText(ColorUtil.toCssColor(hiConfigEntry.getColorB()));
		colorForeField.setText(ColorUtil.toCssColor(hiConfigEntry.getColorF()));
	}

	// ==========

	private class ColorButton implements ActionListener {
		private String title;
		private JTextField textField;

		private ColorButton(String title, JTextField textField) {
			this.title = title;
			this.textField = textField;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Color initColor = new Color(ColorUtil.getColorSafe(textField.getText()));
			Color newColor = JColorChooser.showDialog(ConfigPanel.this, title, initColor);
			if (newColor != null) {
				textField.setText(ColorUtil.toCssColor(newColor.getRGB()));
			}
		}
	}

}