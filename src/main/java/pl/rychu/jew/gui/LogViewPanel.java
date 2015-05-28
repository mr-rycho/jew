package pl.rychu.jew.gui;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.Position.Bias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.LogLine;
import pl.rychu.jew.LogLineFull;
import pl.rychu.jew.filter.LogLineFilter;
import pl.rychu.jew.filter.LogLineFilterAll;
import pl.rychu.jew.filter.LogLineThreadFilter;



public class LogViewPanel extends JList<LogLineFull> implements CyclicModelListener
 , KeyListener {

	private static final long serialVersionUID = -6731368974272464443L;

	private static final Logger log = LoggerFactory.getLogger(LogViewPanel.class);

	private static final String ACTION_KEY_TOGGLE_TAIL = "jew.toggleTail";

	private static final String ACTION_KEY_FILTER_TOGGLE_THREAD = "jew.flt.thread";

	private boolean tail;

	private String filterThread;

	private final List<PanelModelChangeListener> listeners
	 = new CopyOnWriteArrayList<>();

	// ---------

	public static LogViewPanel create(final ListModelLog model) {
		final LogViewPanel result = new LogViewPanel();
		result.setFixedCellWidth(600);
		result.setFixedCellHeight(14);
		result.setCellRenderer(new CellRenderer());
		result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		result.setTail(false);

		createActions(result);
		createKeyBindings(result);
		result.addKeyListener(result);

		result.setModel(model);

		return result;
	}

	private static void createActions(final LogViewPanel logViewPanel) {
		final ActionMap actionMap = new ActionMap();
		final ActionMap oldActionMap = logViewPanel.getActionMap();
		actionMap.setParent(oldActionMap);
		logViewPanel.setActionMap(actionMap);

		actionMap.put(ACTION_KEY_TOGGLE_TAIL, new AbstractAction(ACTION_KEY_TOGGLE_TAIL) {
			private static final long serialVersionUID = -1368224947366776200L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					((LogViewPanel)sourceObj).toggleTail();
				}
			}
		});

		actionMap.put(ACTION_KEY_FILTER_TOGGLE_THREAD, new AbstractAction(ACTION_KEY_FILTER_TOGGLE_THREAD) {
			private static final long serialVersionUID = 5681791587445929606L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					((LogViewPanel)sourceObj).toggleThread();
				}
			}
		});
	}

	private static void createKeyBindings(final LogViewPanel logViewPanel) {
		final InputMap inputMap = new InputMap();
		final InputMap oldInputMap = logViewPanel.getInputMap();
		inputMap.setParent(oldInputMap);
		logViewPanel.setInputMap(WHEN_FOCUSED, inputMap);

		inputMap.put(KeyStroke.getKeyStroke('`'), ACTION_KEY_TOGGLE_TAIL);
		inputMap.put(KeyStroke.getKeyStroke('t'), ACTION_KEY_FILTER_TOGGLE_THREAD);
	}

	// -----

	public void addPanelModelChangeListener(final PanelModelChangeListener listener) {
		listeners.add(listener);
	}

	public void removePanelModelChangeListener(final PanelModelChangeListener listener) {
		listeners.remove(listener);
	}

	// ---------

	public boolean isTail() {
		return tail;
	}

	protected void setTail(final boolean tail) {
		this.tail = tail;
		if (tail) {
			tail(getModel().getSize());
		}
		for (final PanelModelChangeListener lsn: listeners) {
			lsn.panelChanged();
		}
	}

	protected void toggleTail() {
		setTail(!isTail());
	}

	protected void toggleThread() {
		setThreadName(getToggleThread());
	}

	protected String getToggleThread() {
		if (filterThread != null) {
			return null;
		} else {
			final ListModelLog model = (ListModelLog)getModel();
			final int view = getView();
			final LogLine logLine = model.getIndexElementAt(view);
			if (logLine != null) {
				final String threadName = logLine.getThreadName();
				if (threadName!=null && !threadName.isEmpty()) {
					return threadName;
				}
			}
			return filterThread;
		}
	}

	protected void setThreadName(final String newName) {
		if ((newName==null&&filterThread!=null) || (newName!=null&&!newName.equals(filterThread))) {
			filterThread = newName;
			createAndSetFilter();
		}
	}

	protected void createAndSetFilter() {
		setFilter(createFilter());
	}

	protected LogLineFilter createFilter() {
		LogLineFilter filter = null;

		if (filterThread != null) {
			final LogLineFilter fThread = new LogLineThreadFilter(filterThread);
			if (filter != null) {
				// TODO filter AND
			} else {
				filter = fThread;
			}
		}

		return filter!=null ? filter : new LogLineFilterAll();
	}

	protected void setFilter(final LogLineFilter filter) {
		final ListModelLog model = (ListModelLog)getModel();
		final int view = getView();
		final long rootIndex = model.getRootIndex(view);
		model.setFiltering(rootIndex, filter);
	}

	private int getView() {
		final int selectedIndex = getSelectedIndex();
		if (selectedIndex >= 0) {
			return selectedIndex;
		}
		return getFirstVisibleIndex();
	}

	// -----

	private int prevSize = 0;

	@Override
	public void linesAddedStart(int numberOfLinesAdded, final long total) {
		if (tail) {
			tail((int)total);
		} else {
			log.trace("{} + {} -> {}", prevSize, numberOfLinesAdded, total);
			scrollForward(numberOfLinesAdded, prevSize);
		}
		prevSize = (int)total;
	}

	@Override
	public void linesAddedEnd(int numberOfLinesAdded, final long total) {
		if (prevSize == 0) {
			setSelectedIndex(0);
		}
		if (tail) {
			tail((int)total);
		}
		prevSize = (int)total;
	}

	@Override
	public void listReset(final boolean sourceReset) {
		ensureIndexIsVisible(0);
		prevSize = 0;
		if (sourceReset) {
			resetView();
		}
	}

	@Override
	public void sourceChanged(long totalSourceLines) {}

	// ----

	private void resetView() {
		setTail(true);
		setThreadName(null);
	}

	// ----

	private void scrollForward(final int linesToScroll, final int prevSize) {
		final int lastVisibleIndex = getLastWholeVisibleIndex();
		final int lastIndex = prevSize>0 ? prevSize-1 : 0;
		final int maxIndex
		 = lastVisibleIndex >= 0
		 ? Math.min(lastVisibleIndex, lastIndex)
		 : lastIndex;
		final int index = maxIndex + linesToScroll;
		log.trace("{} + {} -> {}", lastVisibleIndex, linesToScroll, index);

		for (int retry=0; retry<5; retry++) {
			final boolean mustRetry = scrollToAndCheck(index);
			if (!mustRetry) {
				break;
			} else {
				log.trace("retrying... (selection is {})", getMinSelectionIndex());
			}
		}
	}

	private boolean scrollToAndCheck(final int index) {
		ensureIndexIsVisible(index);

		final int newFirstVisibleIndex = getFirstVisibleIndex();
		final int newLastVisibleIndex = getLastWholeVisibleIndex();
		log.trace("new visible indexes: {}-{}", newFirstVisibleIndex
		 , newLastVisibleIndex);
		return newFirstVisibleIndex<0 || newLastVisibleIndex<0
		 || index<newFirstVisibleIndex || index>newLastVisibleIndex;
	}

	// ----

	private void tail(final int size) {
		setSelectedIndex(size-1);
		ensureIndexIsVisible(size - 1);
	}

	// ----

	private int getLastWholeVisibleIndex() {
		final int last = getLastVisibleIndex();
		if (last <= 0) {
			return last;
		} else {
			final Rectangle r = getVisibleRect();
	    final Rectangle bounds = getCellBounds(last, last);
			final Rectangle cut = bounds.getBounds();
			SwingUtilities.computeIntersection(r.x, r.y, r.width, r.height, cut);
			if (cut.height != bounds.height) {
				return last - 1;
			} else {
				return last;
			}
		}
	}

	@Override
	public int getNextMatch(String prefix, int startIndex, Bias bias) {
		return -1;
	}

	// -------------

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyPressed(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		final boolean noMod = !e.isAltDown() && !e.isAltGraphDown() && !e.isControlDown()
		 && !e.isMetaDown() && !e.isShiftDown();

		if (noMod && (keyCode==KeyEvent.VK_UP || keyCode==KeyEvent.VK_PAGE_UP
		 || keyCode==KeyEvent.VK_HOME)) {
			if (tail) {
				setTail(false);
			}
		}
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
