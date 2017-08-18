package pl.rychu.jew.gui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractListModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.filter.LogLineFilterChain;
import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.gui.mapper.Mapper;
import pl.rychu.jew.logaccess.LogAccess;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;



public class ListModelLog extends AbstractListModel<LogLineFull> {

	private static final long serialVersionUID = 5990060914470736065L;

	private static final Logger log = LoggerFactory.getLogger(ListModelLog.class);

	private LogAccess logAccess;
	private int logAccessVersion;

	private final Mapper mapper = Mapper.create(65536);

	private final List<CyclicModelListener> listeners
	 = new CopyOnWriteArrayList<>();

	private ModelPopulator modelPopulator;

	private final List<PanelModelChangeListener> pmcLsns
	 = new CopyOnWriteArrayList<>();

	// ------------------

	private ListModelLog(final LogAccess logAccess) {
		this.logAccess = logAccess;
		this.logAccessVersion = -1;
	}

	public static ListModelLog create(final LogAccess logAccess) {
		return new ListModelLog(logAccess);
	}

	void addCyclicModelListener(final CyclicModelListener lsn) {
		listeners.add(lsn);
	}

	void addPanelModelChangeListener(final PanelModelChangeListener listener) {
		pmcLsns.add(listener);
	}

	public void setModelPopulator(ModelPopulator modelPopulator) {
		this.modelPopulator = modelPopulator;
	}

	// TODO move this to populator or to separate FilterManager class
	void setFiltering(long startIndex, LogLineFilterChain filterChain) {
		for (final PanelModelChangeListener lsn: pmcLsns) {
			lsn.modelChanged(filterChain);
		}

		modelPopulator.reconfig(filterChain, startIndex, logAccessVersion);
	}

	// -----------

	@Override
	public int getSize() {
		return (int)(mapper.sizeB() + mapper.sizeF());
	}

	public long getRootIndex(final long index) {
		if (index>=0 && index<mapper.size()) {
			final long indexWithOffset = index - mapper.sizeB();
			return mapper.get(indexWithOffset);
		} else {
			return 0L;
		}
	}

	long getViewIndex(long rootIndex) {
		return mapper.getInv(rootIndex);
	}

	LogLine getIndexElementAt(final int index) {
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

	public void clear(final int newSourceVer, final boolean sourceReset) {
		log.debug("reset start; version {} => {} ;new source={}", logAccessVersion, newSourceVer, sourceReset);
		mapper.clear();
		fireIntervalRemoved(this, 0, Integer.MAX_VALUE>>1);
		for (final CyclicModelListener listener: listeners) {
			listener.listReset(sourceReset);
		}
		logAccessVersion = newSourceVer;
		log.debug("reset end");
	}

	void addF(final long[] values, final int length) {
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

	void addB(final long[] values, final int length) {
		if (length==0) return;

		for (int i=0; i<length; i++) {
			mapper.addB(values[i]);
		}
		fireIntervalAdded(this, 0, length-1);
		for (final CyclicModelListener listener: listeners) {
			listener.linesAddedStart(length, mapper.size());
		}
	}

	void setSourceSize(long size) {
		for (final CyclicModelListener listener: listeners) {
			listener.sourceChanged(size);
		}
	}

}
