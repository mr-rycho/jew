package pl.rychu.jew.gui;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.Position.Bias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.filter.LogLineFilter;
import pl.rychu.jew.filter.LogLineFilterAll;
import pl.rychu.jew.filter.LogLineThreadFilter;
import pl.rychu.jew.gui.hi.HiConfig;
import pl.rychu.jew.gui.hi.HiConfigChangeListener;
import pl.rychu.jew.gui.hi.HiConfigProvider;
import pl.rychu.jew.gui.hi.HiDialog;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;



public class LogViewPanel extends JList<LogLineFull> implements CyclicModelListener
 , KeyListener, MouseWheelListener {

	private static final long serialVersionUID = -6731368974272464443L;

	private static final Logger log = LoggerFactory.getLogger(LogViewPanel.class);

	private static final String ACTION_KEY_TOGGLE_TAIL = "jew.toggleTail";
	private static final String ACTION_KEY_FILTER_TOGGLE_THREAD = "jew.flt.thread";

	private static final String ACTION_KEY_RNDR_TOGGLE_CLASS = "jew.rndr.toggleClass";

	private static final String ACTION_KEY_HI_DIALOG = "jew.hi.dialog";

	private boolean tail;

	private String filterThread;

	private HiConfig hiConfig;

	private HiConfigProvider hiConfigProvider;

	private final List<PanelModelChangeListener> listeners
	 = new CopyOnWriteArrayList<>();

	// ---------

	public static LogViewPanel create(final ListModelLog model) {
		final LogViewPanel result = new LogViewPanel();
		result.setFixedCellWidth(600);
		result.setFixedCellHeight(14);
		result.setCellRenderer(new LogViewPanelCellRenderer());
		result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		result.setTail(false);

		createActions(result);
		createKeyBindings(result);
		result.addKeyListener(result);

		result.addMouseWheelListener(result);

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

		actionMap.put(ACTION_KEY_RNDR_TOGGLE_CLASS, new AbstractAction(ACTION_KEY_RNDR_TOGGLE_CLASS) {
			private static final long serialVersionUID = 3385146847577067253L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					final LogViewPanel logViewPanel = (LogViewPanel)sourceObj;
					LogViewPanelCellRenderer cellRenderer = (LogViewPanelCellRenderer)logViewPanel.getCellRenderer();
					cellRenderer.toggleClassVisuType();
					logViewPanel.repaint();
				}
			}
		});

		actionMap.put(ACTION_KEY_HI_DIALOG, new AbstractAction(ACTION_KEY_HI_DIALOG) {
			private static final long serialVersionUID = -8346845550957257182L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				HiConfigChangeListener lsn = new HiConfigChangeListener() {
					@Override
					public void hiConfigChanged(HiConfig hiConfig) {
						logViewPanel.setHiConfig(hiConfig);
						logViewPanel.hiConfigProvider.put(hiConfig);
						logViewPanel.repaint();
					}
				};
				HiDialog hiDialog
				 = new HiDialog((JFrame)logViewPanel.getTopLevelAncestor(), logViewPanel.hiConfig, lsn);
				// execution continues here after closing the dialog
				hiDialog.dispose();
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
		inputMap.put(KeyStroke.getKeyStroke('C'), ACTION_KEY_RNDR_TOGGLE_CLASS);
		inputMap.put(KeyStroke.getKeyStroke('H'), ACTION_KEY_HI_DIALOG);
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

	public void setHiConfigProvider(HiConfigProvider hiConfigProvider) {
		this.hiConfigProvider = hiConfigProvider;
		setHiConfig(hiConfigProvider.get());
	}

	private void setHiConfig(final HiConfig hiConfig) {
		this.hiConfig = hiConfig;
		((LogViewPanelCellRenderer)getCellRenderer()).setHiConfig(hiConfig);
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

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		boolean isCtrl = (e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK) != 0;
		boolean isShift = (e.getModifiersEx() & MouseWheelEvent.SHIFT_DOWN_MASK) != 0;

		MouseWheelEvent eventToDispatch = e;
		if (isCtrl || isShift) {
			int amountMulti = 1;
			int rotMulti = 1;
			if (isCtrl) {
				amountMulti *= 10;
				if (isShift) {
					amountMulti *= 5;
					rotMulti *= 2;
				}
			}
			eventToDispatch = new MouseWheelEvent(this, e.getID(), e.getWhen()
			 , e.getModifiers() | e.getModifiersEx(), e.getX(), e.getY()
			 , e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger()
			 , e.getScrollType(), e.getScrollAmount()*amountMulti, e.getWheelRotation()*rotMulti
			 , e.getPreciseWheelRotation()*amountMulti*rotMulti);
		}

		if (tail) {
			setTail(false);
		}
		getParent().dispatchEvent(eventToDispatch);
	}

	// ==================

	static enum ClassVisuType {
		NORMAL

		, HIDDEN {
			@Override
			public boolean replacesClass() {
				return true;
			}

			@Override
			public String getReplacement(final String className) {
				return "";
			}
		}

		, CLASS {
			@Override
			public boolean replacesClass() {
				return true;
			}

			@Override
			public String getReplacement(final String className) {
				final int dotIndex = className.lastIndexOf('.');
				return dotIndex<0 ? className : className.substring(dotIndex+1);
			}
		};

		public boolean replacesClass() {
			return false;
		}

		public String getReplacement(final String className) {
			throw new UnsupportedOperationException();
		}
	}

}
