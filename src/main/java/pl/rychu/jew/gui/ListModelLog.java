package pl.rychu.jew.gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.LogAccess;
import pl.rychu.jew.LogLine;
import pl.rychu.jew.LogLineFull;
import pl.rychu.jew.filter.LogLineFilter;
import pl.rychu.jew.filter.LogLineFilterAll;
import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.gui.mapper.Mapper;



public class ListModelLog extends AbstractListModel<LogLineFull> {

	private static final long serialVersionUID = 5990060914470736065L;

	private static final Logger log = LoggerFactory.getLogger(ListModelLog.class);

	private LogAccess logAccess;
	private int logAccessVersion;

	private final Mapper mapper = Mapper.create(65536);

	private final List<CyclicModelListener> listeners
	 = new CopyOnWriteArrayList<>();

	private Thread modNotifierThread;

	private final ModelFacade facade = new ModelFacade(this);

	// ------------------

	private ListModelLog(final LogAccess logAccess) {
		this.logAccess = logAccess;
		this.logAccessVersion = -1;
	}

	public static ListModelLog create(final LogAccess logAccess) {
		final ListModelLog result = new ListModelLog(logAccess);

		result.setFiltering(0L, new LogLineFilterAll());

		return result;
	}

	public void addCyclicModelListener(final CyclicModelListener lsn) {
		listeners.add(lsn);
	}

	public void removeCyclicModelListener(final CyclicModelListener lsn) {
		listeners.remove(lsn);
	}

	public void setFiltering(final long startIndex, final LogLineFilter filter) {
		stopModNotifierAndWait();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				clear(logAccessVersion);

				setupModNotifierAndStart(startIndex, filter);
			}
		});
	}

	private void stopModNotifierAndWait() {
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
	}

	private void setupModNotifierAndStart(final long startIndex, final LogLineFilter filter) {
		final ModNotifier modNotifier = new ModNotifier(logAccess, logAccessVersion
		 , startIndex, filter, facade);
		modNotifierThread = new Thread(modNotifier);
		modNotifierThread.start();
	}
	// -----------

	@Override
	public int getSize() {
		return (int)(mapper.sizeB() + mapper.sizeF());
	}

	public long getRootIndex(final long index) {
		final long indexWithOffset = index - mapper.sizeB();
		return mapper.get(indexWithOffset);
	}

	public LogLine getIndexElementAdd(final int index) {
		final long logAccessIndex = getRootIndex(index);
		try {
			return logAccess.get(logAccessIndex, logAccessVersion);
		} catch (BadVersionException e) {
			return null;
		} catch (IndexOutOfBoundsException e) {
			log.error("index out of bound ({}-{} = {})"
			 , index, mapper.sizeB(), logAccessIndex);
			log.error("index out of bound", e);
			return null;
		}
	}

	@Override
	public LogLineFull getElementAt(final int index) {
		final long logAccessIndex = getRootIndex(index);
		try {
			return logAccess.getFull(logAccessIndex, logAccessVersion);
		} catch (BadVersionException e) {
			return null;
		} catch (IndexOutOfBoundsException e) {
			log.error("index out of bound ({}-{} = {})"
			 , index, mapper.sizeB(), logAccessIndex);
			log.error("index out of bound", e);
			return null;
		}
	}

	// -------------

	public void clear(final int newSourceVer) {
		log.debug("reset start; new version is {}", newSourceVer);
		mapper.clear();
		fireIntervalRemoved(this, 0, Integer.MAX_VALUE>>1);
		for (final CyclicModelListener listener: listeners) {
			listener.listReset();
		}
		logAccessVersion = newSourceVer;
		log.debug("reset end");
	}

	public void addF(final long[] values, final int length) {
		if (length==0) return;

		final int oldSize = (int)mapper.size();
		for (int i=0; i<length; i++) {
			mapper.addF(values[i]);
		}
		final int newSize = (int)mapper.size();
		fireIntervalAdded(this, oldSize, newSize-1);
		for (final CyclicModelListener listener: listeners) {
			listener.linesAddedEnd(length, newSize);
		}
	}

	public void addB(final long[] values, final int length) {
		if (length==0) return;

		for (int i=0; i<length; i++) {
			mapper.addB(values[i]);
		}
		fireIntervalAdded(this, 0, length-1);
		for (final CyclicModelListener listener: listeners) {
			listener.linesAddedStart(length, (int)mapper.size());
		}
	}

}
