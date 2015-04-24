package pl.rychu.jew.gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.LogAccess;
import pl.rychu.jew.LogLineFull;
import pl.rychu.jew.gl.BadVersionException;



public class ListModelLog extends AbstractListModel<LogLineFull> {

	private static final long serialVersionUID = 5990060914470736065L;

	private static final Logger log = LoggerFactory.getLogger(ListModelLog.class);

	private LogAccess logAccess;

	private int sourceSize = 0;

	private final AtomicInteger sourceVersion = new AtomicInteger();

	private final List<CyclicModelListener> listeners
	 = new CopyOnWriteArrayList<>();

	private Thread modNotifierThread;

	// ------------------

	private ListModelLog() {}

	public static ListModelLog create(final LogAccess logAccess) {
		final ListModelLog result = new ListModelLog();

		result.setLogAccess(logAccess);

		return result;
	}

	public void addCyclicModelListener(final CyclicModelListener lsn) {
		listeners.add(lsn);
	}

	public void removeCyclicModelListener(final CyclicModelListener lsn) {
		listeners.remove(lsn);
	}

	public void setLogAccess(final LogAccess logAccess) {
		if (modNotifierThread != null) {
			modNotifierThread.interrupt();
			try {
				modNotifierThread.join();
			} catch (InterruptedException e) {
				log.error("Interrupted", e);
				return;
			}
			modNotifierThread = null;
		}
		this.logAccess = logAccess;
		sourceSize = 0;
		sourceVersion.set(0);
		modNotifierThread = new Thread(new ModNotifier());
		modNotifierThread.start();
	}

	public LogAccess getLogAccess() {
		return logAccess;
	}

	// -----------

	@Override
	public int getSize() {
		try {
			return (int)logAccess.size(sourceVersion.get());
		} catch (BadVersionException e) {
			return 0;
		}
	}

	@Override
	public LogLineFull getElementAt(final int index) {
		try {
			return logAccess.getFull(index, sourceVersion.get());
		} catch (BadVersionException e) {
			return null;
		}
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
				try {
					process();
				} catch (RuntimeException e) {
					log.error("error during processing", e);
				} catch (BadVersionException e) {
					// ignore; version reset will be handled in next cycle
				}

				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					break;
				}
			}

			log.debug("quitting gracefully");
		}

		private void process() throws BadVersionException {
			final int version = logAccess.getVersion();
			if (version != sourceVersion.get()) {
				scheduleFileResetNotification();
				sourceVersion.set(version);
				sourceSize = 0;
			}
			final int newSize = (int)logAccess.size(version);
			if (newSize != sourceSize) {
				if (newSize > sourceSize) {
					scheduleFileGrowNotification(sourceSize, newSize);
				}
				sourceSize = newSize;
			}
		}

		private void scheduleFileResetNotification() {
			log.debug("scheduling file reset");
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireIntervalRemoved(this, 0, Integer.MAX_VALUE>>1);
					for (final CyclicModelListener listener: listeners) {
						listener.listReset();
					}
				}
			});
		}

		private void scheduleFileGrowNotification(final int oldSize, final int newSize) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					fireIntervalAdded(this, oldSize, newSize-1);
					for (final CyclicModelListener listener: listeners) {
						listener.linesAdded(newSize);
					}
				}
			});
		}
	}

}