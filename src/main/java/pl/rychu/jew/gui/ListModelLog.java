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

	private volatile int sourceSizeFSw = 0; // update in swing thread
	private volatile int sourceSizeBSw = 0; // update in swing thread

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
		sourceSizeFSw = 0;
		sourceSizeBSw = 0;
		sourceVersion.set(-1);
		modNotifierThread = new Thread(new ModNotifier());
		modNotifierThread.start();
	}

	public LogAccess getLogAccess() {
		return logAccess;
	}

	// -----------

	private int prevSize = -1;

	@Override
	public int getSize() {
		final int result = sourceSizeFSw + sourceSizeBSw;
		if (result != prevSize) {
			log.debug("size={} ({}+{})", result, sourceSizeBSw, sourceSizeFSw);
			prevSize = result;
		}
		return result;
	}

	@Override
	public LogLineFull getElementAt(final int index) {
		final int logAccessIndex = index - sourceSizeBSw;
		final int sourcev = sourceVersion.get();
		try {
			return logAccess.getFull(logAccessIndex, sourcev);
		} catch (BadVersionException e) {
			return null;
		} catch (IndexOutOfBoundsException e) {
			log.error("index out of bound ({}-{} = {})"
			 , index, sourceSizeBSw, logAccessIndex);
			log.debug("size={} ({}+{})", sourceSizeBSw+sourceSizeFSw
			 , sourceSizeBSw, sourceSizeFSw);
			log.debug("source version: {}", sourcev);
			log.error("index out of bound", e);
			return null;
		}
	}

	// ------

	private class ModNotifier implements Runnable {

		private final Logger log = LoggerFactory.getLogger(ModNotifier.class);

		private int sourceSizeF = 0;
		private int sourceSizeB = 0;

		// -------

		ModNotifier() {
			super();
		}

		// -------

		@Override
		public void run() {
			log.debug("running");
			while (!Thread.interrupted()) {
				try {
					process();
				} catch (RuntimeException e) {
					log.error("error during processing", e);
				} catch (BadVersionException e) {
					log.debug("bad version");
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
			final int oldVersion = sourceVersion.get();
			if (version != oldVersion) {
				log.debug("schedule reset; {} != {}", version, oldVersion);
				scheduleFileResetNotification(version);
				sourceSizeF = 0;
				sourceSizeB = 0;
				return;
			}
			final int newSizeF = (int)logAccess.sizeF(version);
			if (newSizeF != sourceSizeF) {
				if (newSizeF > sourceSizeF) {
					scheduleFileGrowNotificationF(sourceSizeF, newSizeF);
				}
				sourceSizeF = newSizeF;
			}
			final int newSizeB = (int)logAccess.sizeB(version);
			if (newSizeB != sourceSizeB) {
				if (newSizeB > sourceSizeB) {
					scheduleFileGrowNotificationB(sourceSizeB, newSizeB);
				}
				sourceSizeB = newSizeB;
			}
		}

		private void scheduleFileResetNotification(final int newVersion) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					log.debug("reset start; new version is {}", newVersion);
					sourceSizeFSw = 0;
					sourceSizeBSw = 0;
					sourceVersion.set(newVersion);
					fireIntervalRemoved(this, 0, Integer.MAX_VALUE>>1);
					for (final CyclicModelListener listener: listeners) {
						listener.listReset();
					}
					log.debug("reset end");
				}
			});
		}

		private void scheduleFileGrowNotificationF(final int oldSizeF, final int newSizeF) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					sourceSizeFSw = newSizeF;
					fireIntervalAdded(this, sourceSizeBSw+oldSizeF, sourceSizeBSw+newSizeF-1);
					for (final CyclicModelListener listener: listeners) {
						listener.linesAddedEnd(newSizeF-oldSizeF, sourceSizeBSw+sourceSizeFSw);
					}
				}
			});
		}

		private void scheduleFileGrowNotificationB(final int oldSizeB, final int newSizeB) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					sourceSizeBSw = newSizeB;
					fireIntervalAdded(this, 0, newSizeB-oldSizeB-1);
					for (final CyclicModelListener listener: listeners) {
						listener.linesAddedStart(newSizeB-oldSizeB, sourceSizeBSw+sourceSizeFSw);
					}
				}
			});
		}
	}

}
