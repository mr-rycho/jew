package pl.rychu.jew.gui.pars;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created on 21.07.2017.
 */
public class ParsDialog extends JDialog {

	private static final long serialVersionUID = -3576162258828903040L;

	private static final String ACTION_KEY_GLOB_ENTER = "jew.pars.enter";
	private static final String ACTION_KEY_GLOB_ESC = "jew.pars.esc";

	private final ParsConfigSaveListener saveLsn;
	private final PickForParseListener pfpLsn;
	private final ParsConfig origParsConfig;
	private final DefaultListModel<ParsConfigEntry> model;
	private final JList<ParsConfigEntry> jList;
	private final JButton dupNewButton;
	private final ParsEditPanel parsEditPanel;
	private final JLabel parsResultLabel;
	private final String theLine;

	// ----------

	public ParsDialog(JFrame fr, ParsConfig parsConfig, String currentLine,
	 ParsConfigSaveListener saveLsn, PickForParseListener pfpLsn) {
		super(fr, "Parse Dialog", true);

		this.saveLsn = saveLsn;
		this.pfpLsn = pfpLsn;
		this.origParsConfig = ParsConfig.clone(parsConfig);
		this.theLine = currentLine;

		setSize(700, 450);
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout(5, 5));

		model = createModel(parsConfig);
		jList = new JList<>(model);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.setFixedCellHeight(14);
		jList.setCellRenderer(new CellRenderer());
		jList.setPreferredSize(new Dimension(0, 60));

		JPanel parsedPanel = new JPanel();
		parsedPanel.setLayout(new BorderLayout());
		{
			JLabel label = new JLabel(currentLine);
			parsedPanel.add(label, BorderLayout.NORTH);
			parsResultLabel = new JLabel("tu bendzie sparsowane");
			parsedPanel.add(parsResultLabel, BorderLayout.SOUTH);
		}
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout(5, 5));
		topPanel.add(new JScrollPane(jList), BorderLayout.NORTH);
		topPanel.add(parsedPanel, BorderLayout.SOUTH);

		createActions((JComponent) cp);
		ActionMap am = ((JComponent) cp).getActionMap();

		parsEditPanel = new ParsEditPanel();
		JPanel editButtonsPanel = new JPanel();
		editButtonsPanel.setPreferredSize(new Dimension(0, 40));
		editButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JPanel windowButtonsPanel = new JPanel();
		windowButtonsPanel.setPreferredSize(new Dimension(0, 40));
		windowButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BorderLayout());
		buttonsPanel.add(editButtonsPanel, BorderLayout.NORTH);
		buttonsPanel.add(windowButtonsPanel, BorderLayout.SOUTH);

		cp.add(topPanel, BorderLayout.NORTH);
		cp.add(parsEditPanel, BorderLayout.CENTER);
		cp.add(buttonsPanel, BorderLayout.SOUTH);

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

		JButton cancelButton = new JButton("cancel");
		cancelButton.setAction(am.get(ACTION_KEY_GLOB_ESC));
		cancelButton.setText("cancel");
		windowButtonsPanel.add(cancelButton);
		JButton applyButton = new JButton("apply");
		applyButton.addActionListener(e -> notifyParsEntryPicked());
		windowButtonsPanel.add(applyButton);
		JButton pickButton = new JButton("Pick");
		pickButton.addActionListener(e -> {
			notifyParsEntryPicked();
			setVisible(false);
		});
		windowButtonsPanel.add(pickButton);
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> notifyParsConfigSave(listToParsConfig()));
		windowButtonsPanel.add(saveButton);
		JButton saveAndPickButton = new JButton("Save & Pick");
		saveAndPickButton.setAction(am.get(ACTION_KEY_GLOB_ENTER));
		saveAndPickButton.setText("Save & Pick");
		windowButtonsPanel.add(saveAndPickButton);

		jList.addListSelectionListener(new SelectionToConfig(parsEditPanel));
		parsEditPanel.addParsEntryChangeListener(new ConfigToSelection(parsEditPanel));

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		createKeyBindings((JComponent) cp);

		parseLine();

		setVisible(true);
	}

	private ParsConfig listToParsConfig() {
		int size = model.size();
		List<ParsConfigEntry> entries = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			entries.add(model.get(i));
		}
		return new ParsConfig(entries);
	}

	private static DefaultListModel<ParsConfigEntry> createModel(ParsConfig parsConfig) {
		DefaultListModel<ParsConfigEntry> result = new DefaultListModel<>();
		fillModel(result, parsConfig);
		return result;
	}

	private static void fillModel(DefaultListModel<ParsConfigEntry> model, ParsConfig parsConfig) {
		model.clear();
		int size = parsConfig.size();
		for (int i = 0; i < size; i++) {
			model.addElement(parsConfig.get(i));
		}
	}

	private void updateDupNewButton() {
		dupNewButton.setText(model.isEmpty() ? "new" : "duplicate");
	}

	// =======================

	private void createActions(JComponent jp) {
		final ActionMap actionMap = new ActionMap();
		final ActionMap oldActionMap = jp.getActionMap();
		actionMap.setParent(oldActionMap);
		jp.setActionMap(actionMap);

		actionMap.put(ACTION_KEY_GLOB_ENTER, new AbstractAction(ACTION_KEY_GLOB_ENTER) {
			private static final long serialVersionUID = 6718276366669100156L;

			@Override
			public void actionPerformed(ActionEvent e) {
				notifyParsConfigSave(listToParsConfig());
				notifyParsEntryPicked();
				setVisible(false);
			}
		});

		actionMap.put(ACTION_KEY_GLOB_ESC, new AbstractAction(ACTION_KEY_GLOB_ESC) {
			private static final long serialVersionUID = -3740814541749273051L;

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

		inputMap.put(KeyStroke.getKeyStroke("pressed ENTER"), ACTION_KEY_GLOB_ENTER);
		inputMap.put(KeyStroke.getKeyStroke("pressed ESCAPE"), ACTION_KEY_GLOB_ESC);
	}

	private void parseLine() {
		parsResultLabel.setText(parseTheLine());
	}

	private String parseTheLine() {
		if (theLine == null || theLine.isEmpty()) {
			return "[line is empty]";
		}
		ParsConfigEntry pce = parsEditPanel.get();
		try {
			Pattern pattern = pce.getCompiledPattern();
			Matcher matcher = pattern.matcher(theLine);
			if (!matcher.matches()) {
				return "[no match]";
			} else {
				if (pce.getGroupThread() <= 0) {
					return "[thread group not assigned]";
				} else {
					return "thread: " + matcher.group(pce.getGroupThread());
				}
			}
		} catch (PatternSyntaxException e) {
			return e.getMessage();
		}
	}

	private void notifyParsConfigSave(ParsConfig pc) {
		if (saveLsn != null) {
			saveLsn.savingParsConfig(pc);
		}
	}

	private void notifyParsEntryPicked() {
		int index = jList.getMinSelectionIndex();
		if (index >= 0) {
			notifyParsEntryPicked(model.get(index));
		}
	}

	private void notifyParsEntryPicked(ParsConfigEntry pce) {
		if (pfpLsn != null) {
			pfpLsn.parsEntryPicked(pce);
		}
	}

	// =======================

	private class ListActionDupNew implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (model.isEmpty()) {
				model.add(0, new ParsConfigEntry("", "", 1, 2, 3, 4, 5));
				jList.setSelectedIndex(0);
			} else {
				int index = jList.getMinSelectionIndex();
				if (index >= 0) {
					model.add(index + 1, model.get(index));
				}
			}
			updateDupNewButton();
		}
	}

	private class ListActionMove implements ActionListener {
		private final int offset;

		private ListActionMove(int offset) {
			if (offset != 1 && offset != -1) {
				throw new IllegalArgumentException("bad offset: " + offset);
			}
			this.offset = offset;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!model.isEmpty()) {
				int size = model.size();
				int index = jList.getMinSelectionIndex();
				if (index >= 0) {
					if ((offset > 0 && index + 1 < size) || (offset < 0 && index > 0)) {
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

	private class Undoer implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fillModel(model, origParsConfig);
			updateDupNewButton();
		}
	}

	// =======================

	private class SelectionToConfig implements ListSelectionListener {
		private final ParsEditPanel parsEditPanel;

		private SelectionToConfig(ParsEditPanel parsEditPanel) {
			this.parsEditPanel = parsEditPanel;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int minSelIndex = jList.getMinSelectionIndex();
			if (minSelIndex < 0) {
				parsEditPanel.clear();
			} else {
				parsEditPanel.put(jList.getModel().getElementAt(minSelIndex));
			}
			parseLine();
		}
	}

	// =======================

	private class ConfigToSelection implements ParsEntryChangeListener {
		private final ParsEditPanel parsEditPanel;

		private ConfigToSelection(ParsEditPanel parsEditPanel) {
			this.parsEditPanel = parsEditPanel;
		}

		@Override
		public void parsEntryChanged(boolean fromInside) {
			if (fromInside) {
				int index = jList.getMinSelectionIndex();
				if (index >= 0) {
					DefaultListModel<ParsConfigEntry> model = (DefaultListModel<ParsConfigEntry>) jList
					 .getModel();
					ParsConfigEntry parsConfigEntry = parsEditPanel.get();
					model.set(index, parsConfigEntry);
				}
			}
			parseLine();
		}
	}

	// =======================

	private static class CellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 5002424923342503398L;

		private static final Border SEL_BORDER_FOCUS = new LineBorder(Color.BLACK, 1);
		private static final Border SEL_BORDER_NO_FOCUS = new LineBorder(Color.LIGHT_GRAY, 1);

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
		 final boolean isSelected, boolean cellHasFocus) {
			return getListCellRendererComponentSuper(list, value, index, isSelected, cellHasFocus);
		}

		Component getListCellRendererComponentSuper(JList<?> list, Object valueObj, int index,
		 boolean isSelected, boolean cellHasFocus) {

			ParsConfigEntry value = (ParsConfigEntry) valueObj;

			setComponentOrientation(list.getComponentOrientation());

			setIcon(null);
			setText(value.getName() + " : " + value.getPattern());

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
