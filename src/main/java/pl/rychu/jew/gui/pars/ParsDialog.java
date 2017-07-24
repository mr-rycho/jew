package pl.rychu.jew.gui.pars;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Created on 21.07.2017.
 */
public class ParsDialog extends JDialog {

	private static final long serialVersionUID = -3576162258828903040L;

	private final ParsConfigChangeListener lsn;
	private final ParsConfig origParsConfig;
	private final DefaultListModel<ParsConfigEntry> model;
	private final JList<ParsConfigEntry> jList;

	// ----------

	public ParsDialog(JFrame fr, ParsConfig parsConfig, ParsConfigChangeListener lsn) {
		super(fr, "Parse Dialog", true);

		this.lsn = lsn;
		origParsConfig = ParsConfig.clone(parsConfig);

		setSize(450, 400);
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		model = createModel(parsConfig);
		jList = new JList<>(model);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.setFixedCellHeight(14);
		jList.setCellRenderer(new CellRenderer());
		cp.add(new JScrollPane(jList), BorderLayout.CENTER);


		// TODO uncomment
		// setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setVisible(true);
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

	// =======================

	private static class CellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 5002424923342503398L;

		private static final Border SEL_BORDER_FOCUS = new LineBorder(Color.BLACK, 1);
		private static final Border SEL_BORDER_NO_FOCUS = new LineBorder(Color.LIGHT_GRAY, 1);

		@Override
		public Component getListCellRendererComponent(JList<?> list,  Object value
		 ,  int index, final boolean isSelected,  boolean cellHasFocus) {
			return getListCellRendererComponentSuper(list, value, index, isSelected, cellHasFocus);
		}

		Component getListCellRendererComponentSuper(JList<?> list, Object valueObj,
		 int index, boolean isSelected, boolean cellHasFocus) {

			 ParsConfigEntry value = (ParsConfigEntry)valueObj;

			setComponentOrientation(list.getComponentOrientation());

			setIcon(null);
			setText(value.getName()+" : "+value.getPattern());

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
