package pl.rychu.jew.gui;

import javax.swing.AbstractListModel;
import javax.swing.JList;

import pl.rychu.jew.LogFileAccess;
import pl.rychu.jew.LogLine;

public class LogViewPanel extends JList<LogLine> {

	private static final long serialVersionUID = -6731368974272464443L;

	// ---------

	public LogViewPanel(final LogFileAccess logFileAccess) {
		super(new ListModelLog(logFileAccess));
		setFixedCellWidth(600);
		setFixedCellHeight(14);
	}

	// ------

	private static class ListModelLog extends AbstractListModel<LogLine> {

		private static final long serialVersionUID = 5990060914470736065L;

		private final LogFileAccess logFileAccess;

		public ListModelLog(final LogFileAccess logFileAccess) {
			this.logFileAccess = logFileAccess;
		}

		@Override
		public int getSize() {
			final int result = (int)logFileAccess.size();
			System.out.println("size: "+result);
			return result;
		}

		@Override
		public LogLine getElementAt(final int index) {
			System.out.println("access to "+index);
			return logFileAccess.get(index);
		}
	}

}
