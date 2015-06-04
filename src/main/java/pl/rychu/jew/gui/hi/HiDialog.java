package pl.rychu.jew.gui.hi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;




public class HiDialog extends JDialog {

	private static final long serialVersionUID = -2045018991383910432L;

	private final Container cp;

	public HiDialog(final JFrame fr, final HiConfig hiConfig) {
		super(fr, "Highlighting", true);

		setSize(400, 400);

		cp = getContentPane();

		cp.setLayout(new BorderLayout());

		final DefaultListModel<HiConfigEntry> model = createModel(hiConfig);
		final JList<HiConfigEntry> jList = new JList<HiConfigEntry>(model);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.setCellRenderer(new CellRenderer());
		cp.add(jList, BorderLayout.CENTER);

		final ConfigPanel configPanel = new ConfigPanel();

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(0, 40));
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setMinimumSize(new Dimension(300, 40));
		bottomPanel.add(configPanel, BorderLayout.NORTH);
		bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

		cp.add(bottomPanel, BorderLayout.SOUTH);

		final JButton closeButton = new JButton("close");
		closeButton.addActionListener(new Disposer());
		buttonPanel.add(closeButton);

		jList.addListSelectionListener(new SelectionToConfig(jList, configPanel));

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setVisible(true);
	}

	private static DefaultListModel<HiConfigEntry> createModel(final HiConfig hiConfig) {
		final DefaultListModel<HiConfigEntry> result = new DefaultListModel<>();
		final int size = hiConfig.size();
		for (int i=0; i<size; i++) {
			result.addElement(hiConfig.get(i));
		}
		return result;
	}

	// =======================

	private class Disposer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}

	// =======================

	private class SelectionToConfig implements ListSelectionListener {
		private JList<HiConfigEntry> jList;
		private ConfigPanel configPanel;

		private SelectionToConfig(JList<HiConfigEntry> jList, ConfigPanel configPanel) {
			this.jList = jList;
			this.configPanel = configPanel;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int minSelIndex = jList.getMinSelectionIndex();
			if (minSelIndex < 0) {
				configPanel.clear();
			} else {
				configPanel.put(jList.getModel().getElementAt(minSelIndex));
			}
		}
	}

	// =======================

	private static class CellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 5002424923342503398L;

		private static final Border SEL_BORDER_FOCUS = new LineBorder(Color.BLACK, 1);

		private static final Border SEL_BORDER_NO_FOCUS = new LineBorder(Color.LIGHT_GRAY, 1);

		@Override
		public Component getListCellRendererComponent(final JList<?> list, final Object value
		 , final int index, final boolean isSelected, final boolean cellHasFocus) {
			return getListCellRendererComponentSuper(list, value, index, isSelected, cellHasFocus);
		}

		public Component getListCellRendererComponentSuper(final JList<?> list
		 , final Object valueObj, final int index
		 , final boolean isSelected, final boolean cellHasFocus) {

			final HiConfigEntry value = (HiConfigEntry)valueObj;

			setComponentOrientation(list.getComponentOrientation());

			setBackground(new Color(value.getColorB()));
			setForeground(new Color(value.getColorF()));

			setIcon(null);
			setText((value == null) ? "" : value.getRegexp());

			setEnabled(list.isEnabled());
			setFont(list.getFont());

			Border border = noFocusBorder;
			if (isSelected) {
				border = cellHasFocus ? SEL_BORDER_FOCUS : SEL_BORDER_NO_FOCUS;
			}
			setBorder(border);

			return this;
		}
	}
}
