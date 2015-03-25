package pl.rychu.jew.gui;

import java.awt.Component;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import pl.rychu.jew.LogFileAccess;
import pl.rychu.jew.LogFileListener;
import pl.rychu.jew.LogLine;
import pl.rychu.jew.LogLineFull;

public class LogViewPanel extends JList<LogLineFull> {

	private static final long serialVersionUID = -6731368974272464443L;

	// ---------

	public LogViewPanel(final LogFileAccess logFileAccess) {
		super(new ListModelLog(logFileAccess));
		setFixedCellWidth(600);
		setFixedCellHeight(14);
		setCellRenderer(new CellRenderer());
	}

	// ------

	private static class ListModelLog extends AbstractListModel<LogLineFull>
	 implements LogFileListener {

		private static final long serialVersionUID = 5990060914470736065L;

		private final LogFileAccess logFileAccess;

		public ListModelLog(final LogFileAccess logFileAccess) {
			this.logFileAccess = logFileAccess;
		}

		@Override
		public int getSize() {
			final int result = (int)logFileAccess.size();
			return result;
		}

		@Override
		public LogLineFull getElementAt(final int index) {
			return logFileAccess.getFull(index);
		}

		@Override
		public void linesAdded() {
			final int size = (int)logFileAccess.size();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireIntervalAdded(this, size-1, size-1);
				}
			});
		}

		@Override
		public void fileWasReset() {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireIntervalRemoved(this, 0, Integer.MAX_VALUE-1);
				}
			});
		}

	}

	// ------

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

		private String getRenderedString(final LogLine logLine) {
			return logLine.getTimestamp()
			 +" / "+logLine.getLevel()
			 +" / "+"["+logLine.getClassName()+"]"
			 +" / "+"("+logLine.getThreadName()+")";
		}
	}

}
