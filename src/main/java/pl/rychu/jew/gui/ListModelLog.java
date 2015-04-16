package pl.rychu.jew.gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.LogAccess;
import pl.rychu.jew.LogListener;
import pl.rychu.jew.LogLineFull;



public class ListModelLog extends AbstractListModel<LogLineFull>
 implements LogListener {

	private static final long serialVersionUID = 5990060914470736065L;

	private final LogAccess logAccess;

	private final AtomicBoolean mustNotifyReset = new AtomicBoolean(false);

	private final AtomicBoolean mustNotifyInsert = new AtomicBoolean(false);

	private final List<CyclicModelListener> listeners
	 = new CopyOnWriteArrayList<>();

	// ------------------

	private ListModelLog(final LogAccess logAccess) {
		this.logAccess = logAccess;
	}

	public static ListModelLog create(final LogAccess logAccess) {
		final ListModelLog result = new ListModelLog(logAccess);

		logAccess.addLogListener(result);

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
		return (int)logAccess.size();
	}

	@Override
	public LogLineFull getElementAt(final int index) {
		return logAccess.getFull(index);
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
							fireIntervalRemoved(this, 0, Integer.MAX_VALUE>>1);
							for (final CyclicModelListener listener: listeners) {
								listener.listReset();
							}
						}
					});
				}

				if (mustNotifyInsert.getAndSet(false)) {
					final int size = (int)logAccess.size();
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