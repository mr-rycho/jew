package pl.rychu.jew.gui;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.LogFileAccess;
import pl.rychu.jew.LogFileListener;
import pl.rychu.jew.LogLineFull;



public class LogViewPanel extends JList<LogLineFull> {

	private static final long serialVersionUID = -6731368974272464443L;

	private static final Logger log = LoggerFactory.getLogger(LogViewPanel.class);

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
		result.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				final int firstIndex = result.getSelectedIndex();
				infoPanel.setCurrentLine(firstIndex);
			}
		});

		return result;
	}

	// ==================================

	private static class ListModelLog extends AbstractListModel<LogLineFull>
	 implements LogFileListener {

		private static final long serialVersionUID = 5990060914470736065L;

		private final LogFileAccess logFileAccess;

		private final LogViewPanel logViewPanel;

		private final AtomicBoolean mustNotifyReset = new AtomicBoolean(false);

		private final AtomicBoolean mustNotifyInsert = new AtomicBoolean(false);

		private final List<CyclicModelListener> listeners
		 = new CopyOnWriteArrayList<>();

		// ------------------

		private ListModelLog(final LogFileAccess logFileAccess
		 , final LogViewPanel logViewPanel) {
			this.logFileAccess = logFileAccess;
			this.logViewPanel = logViewPanel;
		}

		public static ListModelLog create(final LogFileAccess logFileAccess
		 , final LogViewPanel logViewPanel, final InfoPanel infoPanel) {
			final ListModelLog result = new ListModelLog(logFileAccess, logViewPanel);

			logFileAccess.addLogFileListener(result);

			result.addCyclicModelListener(infoPanel);

			new Thread(result.new ModNotifier()).start();

			return result;
		}

		public void addCyclicModelListener(final CyclicModelListener lsn) {
			listeners.add(lsn);
		}

		public void removeCyclicModelListener(final CyclicModelListener lsn) {
			listeners.remove(lsn);
		}

		@Override
		public int getSize() {
			return (int)logFileAccess.size();
		}

		@Override
		public LogLineFull getElementAt(final int index) {
			return logFileAccess.getFull(index);
		}

		@Override
		public void linesAdded() {
			mustNotifyInsert.set(true);
		}

		@Override
		public void fileWasReset() {
			mustNotifyReset.set(true);
		}

		// ------

		private class ModNotifier implements Runnable {

			private final Logger log = LoggerFactory.getLogger(ModNotifier.class);

			// -------

			ModNotifier() {
				super();
			}

			// -------

			@Override
			public void run() {
				while (!Thread.interrupted()) {
					if (mustNotifyReset.getAndSet(false)) {
						log.debug("scheduling file reset");
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								log.debug("removing");
								logViewPanel.ensureIndexIsVisible(0);
								fireIntervalRemoved(this, 0, Integer.MAX_VALUE>>1);
								log.debug("removed");
								for (final CyclicModelListener listener: listeners) {
									listener.listReset();
								}
							}
						});
					}

					if (mustNotifyInsert.getAndSet(false)) {
						final int size = (int)logFileAccess.size();
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								fireIntervalAdded(this, size-1, size-1);
								for (final CyclicModelListener listener: listeners) {
									listener.linesAdded(size);
								}
							}
						});
					}

					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						break;
					}
				}

				log.debug("quitting gracefully");
			}
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
	}

}
