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

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;




public class HiDialog extends JDialog {

	private static final long serialVersionUID = -2045018991383910432L;

	private static final String ACTION_KEY_GLOB_ENTER = "jew.hi.enter";
	private static final String ACTION_KEY_GLOB_ESC = "jew.hi.esc";

	// -------------

	private final HiConfig origHiConfig;
	private final DefaultListModel<HiConfigEntry> model;
	private final JList<HiConfigEntry> jList;
	private final HiConfigChangeListener lsn;
	private JButton dupNewButton;

	public HiDialog(final JFrame fr, final HiConfig hiConfig
	 , HiConfigChangeListener hiConfigChangeListener) {
		super(fr, "Highlighting", true);

		lsn = hiConfigChangeListener;
		origHiConfig = HiConfig.clone(hiConfig);

		setSize(450, 400);

		Container cp = getContentPane();

		cp.setLayout(new BorderLayout());

		model = createModel(hiConfig);
		jList = new JList<>(model);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.setFixedCellHeight(14);
		jList.setCellRenderer(new CellRenderer());
		cp.add(new JScrollPane(jList), BorderLayout.CENTER);

		createActions((JComponent) cp);
		ActionMap am = ((JComponent) cp).getActionMap();
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

		dupNewButton = new JButton("duplicate");
		dupNewButton.addActionListener(new ListActionDupNew());
		editButtonsPanel.add(dupNewButton);
		updateDupNewButton();
		JButton removeButton = new JButton("remove");
		removeButton.addActionListener(new ListActionRemove());
		editButtonsPanel.add(removeButton);
		JButton moveUpButton = new JButton("up");
		moveUpButton.addActionListener(new ListActionMove(-1));
		editButtonsPanel.add(moveUpButton);
		JButton moveDownButton = new JButton("down");
		moveDownButton.addActionListener(new ListActionMove(1));
		editButtonsPanel.add(moveDownButton);
		JButton undoButton = new JButton("undo");
		undoButton.addActionListener(new Undoer());
		editButtonsPanel.add(undoButton);

		JButton closeButton = new JButton("cancel");
		closeButton.setAction(am.get(ACTION_KEY_GLOB_ESC));
		closeButton.setText("cancel");
		windowButtonsPanel.add(closeButton);
		JButton applyButton = new JButton("apply");
		applyButton.addActionListener(new DialogApplier());
		windowButtonsPanel.add(applyButton);
		final JButton acceptButton = new JButton("OK");
		acceptButton.setAction(am.get(ACTION_KEY_GLOB_ENTER));
		acceptButton.setText("OK");
		windowButtonsPanel.add(acceptButton);

		jList.addListSelectionListener(new SelectionToConfig(configPanel));
		configPanel.addHiEntryChangeListener(new ConfigToSelection(configPanel));

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		createKeyBindings((JComponent) cp);

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

	private void updateDupNewButton() {
		dupNewButton.setText(model.isEmpty() ? "new" : "duplicate");
	}

	private void createActions(JComponent jp) {
		final ActionMap actionMap = new ActionMap();
		final ActionMap oldActionMap = jp.getActionMap();
		actionMap.setParent(oldActionMap);
		jp.setActionMap(actionMap);

		actionMap.put(ACTION_KEY_GLOB_ENTER, new AbstractAction(ACTION_KEY_GLOB_ENTER) {
		private static final long serialVersionUID = 6718276366669100156L;
		@Override
			public void actionPerformed(ActionEvent e) {
				new DialogAccepter().actionPerformed(e);
			}
		});

		actionMap.put(ACTION_KEY_GLOB_ESC, new AbstractAction(ACTION_KEY_GLOB_ESC) {
		private static final long serialVersionUID = -3740814541749273051L;
		@Override
			public void actionPerformed(ActionEvent e) {
				new DialogCloser().actionPerformed(e);
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

	// =======================

	private class ListActionDupNew implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (model.isEmpty()) {
				model.add(0, new HiConfigEntry("", 0xffffff, 0x000000));
				jList.setSelectedIndex(0);
			} else {
				int index = jList.getMinSelectionIndex();
				if (index >= 0) {
					model.add(index+1, model.get(index));
				}
			}
			updateDupNewButton();
		}
	}

	private class ListActionMove implements ActionListener {
		private final int offset;

		private ListActionMove(int offset) {
			if (offset!=1 && offset!=-1) {
				throw new IllegalArgumentException("bad offset: "+offset);
			}
			this.offset = offset;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!model.isEmpty()) {
				int size = model.size();
				int index = jList.getMinSelectionIndex();
				if (index >= 0) {
					if ((offset>0 && index+1<size) || (offset<0 && index>0)) {
						int targetIndex = index + offset;
						model.add(targetIndex, model.remove(index));
						jList.getSelectionModel().addSelectionInterval(targetIndex, targetIndex);
					}
				}
			}
		}
	}

	private class ListActionRemove implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int index = jList.getMinSelectionIndex();
			if (index >= 0) {
				model.remove(index);
			}
			updateDupNewButton();
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

	private class DialogApplier implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (lsn != null) {
				lsn.hiConfigChanged(listToHiConfig());
			}
		}
	}

	private class Undoer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fillModel(model, origHiConfig);
			updateDupNewButton();
		}
	}

	// =======================

	private class SelectionToConfig implements ListSelectionListener {
		private final ConfigPanel configPanel;

		private SelectionToConfig(ConfigPanel configPanel) {
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
		private final ConfigPanel configPanel;

		private ConfigToSelection(ConfigPanel configPanel) {
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

		Component getListCellRendererComponentSuper(JList<?> list, Object valueObj,
		 int index, boolean isSelected, boolean cellHasFocus) {

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
