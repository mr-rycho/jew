package pl.rychu.jew.gui.hi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
	private final HiConfig origHiConfig;
	private final DefaultListModel<HiConfigEntry> model;
	private final HiConfigChangeListener lsn;

	public HiDialog(final JFrame fr, final HiConfig hiConfig
	 , HiConfigChangeListener hiConfigChangeListener) {
		super(fr, "Highlighting", true);

		lsn = hiConfigChangeListener;
		origHiConfig = HiConfig.clone(hiConfig);

		setSize(400, 400);

		cp = getContentPane();

		cp.setLayout(new BorderLayout());

		model = createModel(hiConfig);
		final JList<HiConfigEntry> jList = new JList<HiConfigEntry>(model);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.setCellRenderer(new CellRenderer());
		cp.add(jList, BorderLayout.CENTER);

		final ConfigPanel configPanel = new ConfigPanel();

		final JPanel editButtonsPanel = new JPanel();
		editButtonsPanel.setPreferredSize(new Dimension(0, 40));
		editButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		final JPanel windowButtonsPanel = new JPanel();
		windowButtonsPanel.setPreferredSize(new Dimension(0, 40));
		windowButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BorderLayout());
		buttonsPanel.add(editButtonsPanel, BorderLayout.NORTH);
		buttonsPanel.add(windowButtonsPanel, BorderLayout.SOUTH);

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setMinimumSize(new Dimension(300, 40));
		bottomPanel.add(configPanel, BorderLayout.NORTH);
		bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

		cp.add(bottomPanel, BorderLayout.SOUTH);

		JButton duplicateButton = new JButton("duplicate");
		editButtonsPanel.add(duplicateButton);
		JButton removeButton = new JButton("remove");
		removeButton.addActionListener(new ListActionRemove(jList));
		editButtonsPanel.add(removeButton);

		final JButton undoButton = new JButton("undo");
		undoButton.addActionListener(new Undoer());
		windowButtonsPanel.add(undoButton);
		final JButton cancelButton = new JButton("cancel");
		cancelButton.addActionListener(new DialogCloser());
		windowButtonsPanel.add(cancelButton);
		final JButton acceptButton = new JButton("OK");
		acceptButton.addActionListener(new DialogAccepter());
		windowButtonsPanel.add(acceptButton);

		jList.addListSelectionListener(new SelectionToConfig(jList, configPanel));
		configPanel.addHiEntryChangeListener(new ConfigToSelection(jList, configPanel));

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setVisible(true);
	}

	private HiConfig listToHiConfig() {
		int size = model.size();
		List<HiConfigEntry> entries = new ArrayList<>(size);
		for (int i=0; i<size; i++) {
			entries.add(model.get(i));
		}
		return new HiConfig(entries);
	}

	private static DefaultListModel<HiConfigEntry> createModel(final HiConfig hiConfig) {
		final DefaultListModel<HiConfigEntry> result = new DefaultListModel<>();
		fillModel(result, hiConfig);
		return result;
	}

	private static void fillModel(DefaultListModel<HiConfigEntry> model, final HiConfig hiConfig) {
		model.clear();
		final int size = hiConfig.size();
		for (int i=0; i<size; i++) {
			model.addElement(hiConfig.get(i));
		}
	}

	// =======================

	private class ListActionRemove implements ActionListener {
		private final JList<HiConfigEntry> jList;

		private ListActionRemove(JList<HiConfigEntry> jList) {
			this.jList = jList;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int index = jList.getMinSelectionIndex();
			if (index >= 0) {
				model.remove(index);
			}
		}
	}

	private class DialogCloser implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}

	private class DialogAccepter implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (lsn != null) {
				lsn.hiConfigChanged(listToHiConfig());
			}
			setVisible(false);
		}
	}

	private class Undoer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fillModel(model, origHiConfig);
		}
	}

	// =======================

	private class SelectionToConfig implements ListSelectionListener {
		private final JList<HiConfigEntry> jList;
		private final ConfigPanel configPanel;

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

	private class ConfigToSelection implements HiEntryChangeListener {
		private final JList<HiConfigEntry> jList;
		private final ConfigPanel configPanel;

		private ConfigToSelection(JList<HiConfigEntry> jList, ConfigPanel configPanel) {
			this.jList = jList;
			this.configPanel = configPanel;
		}

		@Override
		public void hiEntryChanged(boolean fromInside) {
			if (fromInside) {
				int index = jList.getMinSelectionIndex();
				if (index >= 0) {
					DefaultListModel<HiConfigEntry> model
					 = (DefaultListModel<HiConfigEntry>)jList.getModel();
					HiConfigEntry hiConfigEntry = configPanel.get();
					model.set(index, hiConfigEntry);
				}
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
