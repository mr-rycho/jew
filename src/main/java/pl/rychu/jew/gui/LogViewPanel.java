package pl.rychu.jew.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import pl.rychu.jew.LogFileAccess;
import pl.rychu.jew.LogLineFull;



public class LogViewPanel extends JList<LogLineFull> implements CyclicModelListener {

	private static final long serialVersionUID = -6731368974272464443L;

	// ---------

	public static LogViewPanel create(final LogFileAccess logFileAccess
	 , final InfoPanel infoPanel) {
		final LogViewPanel result = new LogViewPanel();
		result.setFixedCellWidth(600);
		result.setFixedCellHeight(14);
		result.setCellRenderer(new CellRenderer());
		result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		final ListModelLog model = ListModelLog.create(logFileAccess, result, infoPanel);

		result.setModel(model);

		return result;
	}

	@Override
	public void linesAdded(int newSize) {}

	@Override
	public void listReset() {
		ensureIndexIsVisible(0);
	}

	// ==================

	private static class CellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 7313136726313412175L;

		@Override
    public Component getListCellRendererComponent(final JList<?> list
     , final Object value, final int index, final boolean isSelected
     , final boolean cellHasFocus) {
			final LogLineFull logLineFull = (LogLineFull)value;
			final String logLineStr = getRenderedString(logLineFull);
			return super.getListCellRendererComponent(list, logLineStr, index
			 , isSelected, cellHasFocus);
		}

		private String getRenderedString(final LogLineFull logLineFull) {
			if (logLineFull == null) {
				return "~";
			} else {
				return logLineFull.getFullText();
			}
		}
	}

}
