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



public class ListModelLog extends AbstractListModel<LogLineFull> {

	private static final long serialVersionUID = 5990060914470736065L;

	private final LogAccess logAccess;

	private int sourceSize = 0;

	private final AtomicInteger sourceVersion = new AtomicInteger();

	private final List<CyclicModelListener> listeners
	 = new CopyOnWriteArrayList<>();

	// ------------------

	private ListModelLog(final LogAccess logAccess) {
		this.logAccess = logAccess;
	}

	public static ListModelLog create(final LogAccess logAccess) {
		final ListModelLog result = new ListModelLog(logAccess);

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
		return (int)logAccess.size(sourceVersion.get());
	}

	@Override
	public LogLineFull getElementAt(final int index) {
		return logAccess.getFull(index, sourceVersion.get());
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
				}
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					break;
				}
			}

			log.debug("quitting gracefully");
		}

		private void process() {
			final int version = logAccess.getVersion();
			if (version != sourceVersion.get()) {
				scheduleFileResetNotification();
				sourceVersion.set(version);
				sourceSize = 0;
			} else {
				final int newSize = (int)logAccess.size(version);
				if (newSize != sourceSize) {
					if (newSize > sourceSize) {
						scheduleFileGrowNotification(sourceSize, newSize);
					}
					sourceSize = newSize;
				}
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