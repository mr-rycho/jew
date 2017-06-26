package pl.rychu.jew.gui;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Position.Bias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.filter.LogLineFilter;
import pl.rychu.jew.filter.LogLineFilterAll;
import pl.rychu.jew.filter.LogLineFilterAnd;
import pl.rychu.jew.filter.LogLineFilterPos;
import pl.rychu.jew.filter.LogLineFilterStackCollapse;
import pl.rychu.jew.filter.LogLineFilterStackShort;
import pl.rychu.jew.filter.LogLineThreadFilter;
import pl.rychu.jew.gui.hi.HiConfig;
import pl.rychu.jew.gui.hi.HiConfigChangeListener;
import pl.rychu.jew.gui.hi.HiConfigProvider;
import pl.rychu.jew.gui.hi.HiDialog;
import pl.rychu.jew.gui.search.SearchDialog;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLine.LogLineType;
import pl.rychu.jew.logline.LogLineFull;



public class LogViewPanel extends JList<LogLineFull> implements CyclicModelListener
 , KeyListener, MouseWheelListener, ListSelectionListener, CellRenderedListener {

	private static final long serialVersionUID = -6731368974272464443L;

	private static final Logger log = LoggerFactory.getLogger(LogViewPanel.class);

	private static final String ACTION_KEY_TOGGLE_TAIL = "jew.toggleTail";

	private static final String ACTION_KEY_TOGGLE_THREAD_IN_LIST = "jew.togThreadInList";

	private static final String ACTION_KEY_FILTER_TOGGLE_THREAD = "jew.flt.thread";
	private static final String ACTION_KEY_FILTER_TOGGLE_THREAD_LIST = "jew.flt.threadList";
	private static final String ACTION_KEY_FILTER_TOGGLE_STACK = "jew.flt.stack";
	private static final String ACTION_KEY_FILTER_POS_MIN_CURRENT = "jew.flt.pos.min.cur";
	private static final String ACTION_KEY_FILTER_POS_MIN_ZERO = "jew.flt.pos.min.zero";
	private static final String ACTION_KEY_FILTER_POS_MAX_CURRENT = "jew.flt.pos.max.cur";
	private static final String ACTION_KEY_FILTER_POS_MAX_ZERO = "jew.flt.pos.max.zero";

	private static final String ACTION_KEY_RNDR_TOGGLE_CLASS = "jew.rndr.toggleClass";
	private static final String ACTION_KEY_RNDR_TOGGLE_THROFF = "jew.rndr.toggleThroff";

	private static final String ACTION_KEY_HI_DIALOG = "jew.hi.dialog";

	private static final String ACTION_KEY_SEARCH_DIALOG = "jew.search.dialog";
	private static final String ACTION_KEY_SEARCH_AGAIN = "jew.search.again";

	private static final String ACTION_KEY_SAVE_TO_FILE = "jew.saveToFile";

	private static final String ACTION_KEY_CAUSE_NEXT = "jew.cause.next";
	private static final String ACTION_KEY_CAUSE_PREV = "jew.cause.prev";

	private static final String ACTION_KEY_HELP_DIALOG = "jew.help.dialog";

	private boolean tail;

	private String filterThread;
	private final Set<String> filterThreads = new HashSet<>();
	private final Set<String> filterThreadsView = Collections.unmodifiableSet(filterThreads);
	private boolean filterThreadsActive = false;
	private StacktraceShowMode stacktraceShowMode = StacktraceShowMode.SHOW;
	private long minFilePosFilter = 0L;
	private long minLineFilter = 0L;
	private long maxFilePosFilter = Long.MAX_VALUE;
	private long maxLineFilter = Long.MAX_VALUE;

	private HiConfig hiConfig;
	private HiConfigProvider hiConfigProvider;

	private SearchDialog searchDialog;
	private PrevSearch prevSearch = null;

	private SaveToFileDelegate saveToFileDelegate;

	private MessageConsumer messageConsumer;

	private final List<PanelModelChangeListener> listeners
	 = new CopyOnWriteArrayList<>();

	// ---------

	public static LogViewPanel create(final ListModelLog model, String initFilter) {
		final LogViewPanel result = new LogViewPanel();
		result.setFixedCellWidth(600);
		result.setFixedCellHeight(14);
		LogViewPanelCellRenderer cellRenderer = new LogViewPanelCellRenderer();
		result.setCellRenderer(cellRenderer);
		cellRenderer.addCellRenderedListener(result);
		result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		applyInitFilter(result, initFilter);

		result.setTail(false);

		createActions(result);
		createKeyBindings(result);
		result.addKeyListener(result);

		result.addMouseWheelListener(result);

		result.setModel(model);

		result.searchDialog = new SearchDialog((JFrame)result.getTopLevelAncestor());

		result.saveToFileDelegate = result.new SaveToFileDelegate(result);

		result.addListSelectionListener(result);

		return result;
	}

	private static void applyInitFilter(LogViewPanel logViewPanel, String initFilterStr) {
		Map<String, String> initFilterMap = parseInitFilter(initFilterStr);

		StacktraceShowMode stacktraceShowMode = parseStackShowMode(initFilterMap.get("stack"));
		if (stacktraceShowMode != null) {
			logViewPanel.stacktraceShowMode = stacktraceShowMode;
		}
	}

	private static StacktraceShowMode parseStackShowMode(String sm) {
		if (sm==null || sm.isEmpty()) {
			return null;
		}

		try {
			return StacktraceShowMode.valueOf(sm.toUpperCase());
		} catch (Exception e) {
			return null;
		}
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

		actionMap.put(ACTION_KEY_TOGGLE_THREAD_IN_LIST, new AbstractAction(ACTION_KEY_TOGGLE_THREAD_IN_LIST) {
			private static final long serialVersionUID = 3385146847577067253L;
			@Override
				public void actionPerformed(ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					((LogViewPanel)sourceObj).toggleThreadInList();
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

		actionMap.put(ACTION_KEY_FILTER_TOGGLE_THREAD_LIST, new AbstractAction(ACTION_KEY_FILTER_TOGGLE_THREAD_LIST) {
		private static final long serialVersionUID = -330377882983454631L;
		@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					((LogViewPanel)sourceObj).toggleFilterThreadsActive();
				}
			}
		});

		actionMap.put(ACTION_KEY_FILTER_TOGGLE_STACK, new AbstractAction(ACTION_KEY_FILTER_TOGGLE_STACK) {
			private static final long serialVersionUID = 3385146847577067253L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					((LogViewPanel)sourceObj).toggleStacktraceShowMode();
				}
			}
		});

		actionMap.put(ACTION_KEY_FILTER_POS_MIN_CURRENT, new AbstractAction(ACTION_KEY_FILTER_POS_MIN_CURRENT) {
			private static final long serialVersionUID = -330377882983454631L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					((LogViewPanel)sourceObj).setMinFilePosToCurrent();
				}
			}
		});

		actionMap.put(ACTION_KEY_FILTER_POS_MIN_ZERO, new AbstractAction(ACTION_KEY_FILTER_POS_MIN_ZERO) {
			private static final long serialVersionUID = 5948643319788351989L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					((LogViewPanel)sourceObj).setMinFilePosToZero();
				}
			}
		});

		actionMap.put(ACTION_KEY_FILTER_POS_MAX_CURRENT, new AbstractAction(ACTION_KEY_FILTER_POS_MAX_CURRENT) {
			private static final long serialVersionUID = -4880211722881537803L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					((LogViewPanel)sourceObj).setMaxFilePosToCurrent();
				}
			}
		});

		actionMap.put(ACTION_KEY_FILTER_POS_MAX_ZERO, new AbstractAction(ACTION_KEY_FILTER_POS_MAX_ZERO) {
			private static final long serialVersionUID = -197055898380588590L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					((LogViewPanel)sourceObj).setMaxFilePosToZero();
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

		actionMap.put(ACTION_KEY_RNDR_TOGGLE_THROFF, new AbstractAction(ACTION_KEY_RNDR_TOGGLE_THROFF) {
			private static final long serialVersionUID = -1775960056463509375L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					final LogViewPanel logViewPanel = (LogViewPanel)sourceObj;
					LogViewPanelCellRenderer cellRenderer = (LogViewPanelCellRenderer)logViewPanel.getCellRenderer();
					cellRenderer.toggleThreadOffsetMode();
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

		actionMap.put(ACTION_KEY_SEARCH_DIALOG, new AbstractAction(ACTION_KEY_SEARCH_DIALOG) {
			private static final long serialVersionUID = 8001885466600099986L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				SearchDialog sd = logViewPanel.searchDialog;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						sd.setFocusToText();
					}
				});
				sd.setVisible(true);
				// execution continues here after closing the dialog
				if (sd.isOkPressed()) {
					PrevSearch ps = logViewPanel.new PrevSearch(sd.getSearchText()
					 , !sd.isSearchPlaintext(), sd.isDownSearch());
					logViewPanel.prevSearch = ps;
					logViewPanel.search(ps);
				}
			}
		});

		actionMap.put(ACTION_KEY_SEARCH_AGAIN, new AbstractAction(ACTION_KEY_SEARCH_AGAIN) {
			private static final long serialVersionUID = 6045288341802840652L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				PrevSearch ps = logViewPanel.prevSearch;
				logViewPanel.search(ps);
			}
		});

		actionMap.put(ACTION_KEY_SAVE_TO_FILE, new AbstractAction(ACTION_KEY_SAVE_TO_FILE) {
		private static final long serialVersionUID = 1670930291832953057L;
		@Override
			public void actionPerformed(ActionEvent e) {
				logViewPanel.saveToFileDelegate.saveToFile();
			}
		});

		actionMap.put(ACTION_KEY_CAUSE_NEXT, new AbstractAction(ACTION_KEY_CAUSE_NEXT) {
			private static final long serialVersionUID = 1670930291832953057L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				logViewPanel.new StackSearchDelegate().gotoCause(logViewPanel.getView(), true);
			}
		});

		actionMap.put(ACTION_KEY_CAUSE_PREV, new AbstractAction(ACTION_KEY_CAUSE_PREV) {
			private static final long serialVersionUID = 1167187188820770875L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				logViewPanel.new StackSearchDelegate().gotoCause(logViewPanel.getView(), false);
			}
		});

		actionMap.put(ACTION_KEY_HELP_DIALOG, new AbstractAction(ACTION_KEY_HELP_DIALOG) {
			private static final long serialVersionUID = 1670930291832953057L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				new HelpDialog((JFrame)logViewPanel.getTopLevelAncestor());
				// execution continues here after closing the dialog
			}
		});
	}

	private static void createKeyBindings(final LogViewPanel logViewPanel) {
		final InputMap inputMap = new InputMap();
		final InputMap oldInputMap = logViewPanel.getInputMap();
		inputMap.setParent(oldInputMap);
		logViewPanel.setInputMap(WHEN_FOCUSED, inputMap);

		inputMap.put(KeyStroke.getKeyStroke('`'), ACTION_KEY_TOGGLE_TAIL);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK)
		 , ACTION_KEY_TOGGLE_THREAD_IN_LIST);
		inputMap.put(KeyStroke.getKeyStroke('t'), ACTION_KEY_FILTER_TOGGLE_THREAD);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)
		 , ACTION_KEY_FILTER_TOGGLE_THREAD_LIST);
		inputMap.put(KeyStroke.getKeyStroke('S'), ACTION_KEY_FILTER_TOGGLE_STACK);
		inputMap.put(KeyStroke.getKeyStroke('['), ACTION_KEY_FILTER_POS_MIN_CURRENT);
		inputMap.put(KeyStroke.getKeyStroke('{'), ACTION_KEY_FILTER_POS_MIN_ZERO);
		inputMap.put(KeyStroke.getKeyStroke(']'), ACTION_KEY_FILTER_POS_MAX_CURRENT);
		inputMap.put(KeyStroke.getKeyStroke('}'), ACTION_KEY_FILTER_POS_MAX_ZERO);
		inputMap.put(KeyStroke.getKeyStroke('C'), ACTION_KEY_RNDR_TOGGLE_CLASS);
		inputMap.put(KeyStroke.getKeyStroke('T'), ACTION_KEY_RNDR_TOGGLE_THROFF);
		inputMap.put(KeyStroke.getKeyStroke('H'), ACTION_KEY_HI_DIALOG);
		inputMap.put(KeyStroke.getKeyStroke("ctrl pressed F"), ACTION_KEY_SEARCH_DIALOG);
		inputMap.put(KeyStroke.getKeyStroke("pressed F3"), ACTION_KEY_SEARCH_AGAIN);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK)
		 , ACTION_KEY_SAVE_TO_FILE);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, InputEvent.CTRL_MASK)
		 , ACTION_KEY_CAUSE_NEXT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_MASK)
		 , ACTION_KEY_CAUSE_PREV);
		inputMap.put(KeyStroke.getKeyStroke("pressed F1"), ACTION_KEY_HELP_DIALOG);
	}

	// -----

	public void setMessageConsumer(MessageConsumer mc) {
		this.messageConsumer = mc;
	}

	public void addPanelModelChangeListener(final PanelModelChangeListener listener) {
		listeners.add(listener);
	}

	public void removePanelModelChangeListener(final PanelModelChangeListener listener) {
		listeners.remove(listener);
	}

	private void notifyPanelModelChangeListeners() {
		for (final PanelModelChangeListener lsn: listeners) {
			lsn.panelChanged();
		}
	}

	// ---------

	private void sendMessage(String text) {
		if (messageConsumer != null) {
			messageConsumer.enqueueMessage(text);
		}
	}

	// ---------

	private void search(PrevSearch ps) {
		if (ps != null) {
			boolean found = new SearchDelegate().search(ps, getView());
			if (!found) {
				sendMessage("\""+ps.getText()+"\" not found");
			}
		}
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
		notifyPanelModelChangeListeners();
	}

	protected void toggleTail() {
		setTail(!isTail());
	}

	// -----

	private void resetFilterSilent() {
		filterThread = null;
		filterThreads.clear();
		filterThreadsActive = false;
		// stacktraceShowMode = StacktraceShowMode.SHOW;
		minFilePosFilter = 0L;
		minLineFilter = 0L;
		maxFilePosFilter = Long.MAX_VALUE;
		maxLineFilter = Long.MAX_VALUE;
	}

	// -----

	protected void toggleThread() {
		setThreadName(getToggleThread());
	}

	protected String getToggleThread() {
		return filterThread!=null ? null : getCurrentThreadName();
	}

	protected void setThreadName(final String newName) {
		if ((newName==null&&filterThread!=null) || (newName!=null&&!newName.equals(filterThread))) {
			filterThread = newName;
			createAndSetFilter();
		}
	}

	// -----

	private void toggleFilterThreadsActive() {
		if (filterThreadsActive) {
			filterThreadsActive = false;
			createAndSetFilter();
		} else {
			if (!filterThreads.isEmpty()) {
				filterThreadsActive = true;
				createAndSetFilter();
			}
		}
	}

	protected void toggleThreadInList() {
		String threadName = getCurrentThreadName();
		if (threadName != null) {
			toggleThreadInList(threadName);
			if (filterThreadsActive) {
				if (filterThreads.isEmpty()) {
					toggleFilterThreadsActive();
				} else {
					createAndSetFilter();
				}
			}
			notifyPanelModelChangeListeners();
		}
	}

	private void toggleThreadInList(String threadName) {
		if (filterThreads.contains(threadName)) {
			filterThreads.remove(threadName);
		} else {
			filterThreads.add(threadName);
		}
	}

	Set<String> getFilterThreads() {
		return filterThreadsView;
	}

	// -----

	private String getCurrentThreadName() {
		final ListModelLog model = (ListModelLog)getModel();
		final int view = getView();
		final LogLine logLine = model.getIndexElementAt(view);
		if (logLine != null) {
			final String threadName = logLine.getThreadName();
			if (threadName!=null && !threadName.isEmpty()) {
				return threadName;
			}
		}
		return null;
	}

	// -----

	private void toggleStacktraceShowMode() {
		setStacktraceShowMode(getNext(stacktraceShowMode));
	}

	private void setStacktraceShowMode(StacktraceShowMode newMode) {
		if (newMode != stacktraceShowMode) {
			stacktraceShowMode = newMode;
			createAndSetFilter();
		}
	}

	// -----

	private void setMinFilePosToCurrent() {
		final int view = getView();
		long rootLine = getRootLine(view);
		setMinFilePos(getCurrentLineFilePos(view), rootLine);
	}

	private void setMinFilePosToZero() {
		setMinFilePos(0L, 0L);
	}

	private void setMaxFilePosToCurrent() {
		final int view = getView();
		long rootLine = getRootLine(view);
		setMaxFilePos(getCurrentLineFilePos(view), rootLine);
	}

	private void setMaxFilePosToZero() {
		setMaxFilePos(Long.MAX_VALUE, Long.MAX_VALUE);
	}

	private long getRootLine(int view) {
		ListModelLog model = (ListModelLog)getModel();
		return model.getRootIndex(view);
	}

	private long getCurrentLineFilePos(int view) {
		final ListModelLog model = (ListModelLog)getModel();
		final LogLine logLine = model.getIndexElementAt(view);
		if (logLine != null) {
			return logLine.getFilePos();
		}
		return 0L;
	}

	private void setMinFilePos(long newMinFilePos, long newMinLine) {
		if (newMinFilePos!=minFilePosFilter || newMinLine!=minLineFilter) {
			minFilePosFilter = newMinFilePos;
			minLineFilter = newMinLine;
			createAndSetFilter();
		}
	}

	private void setMaxFilePos(long newMaxFilePos, long newMaxLine) {
		if (newMaxFilePos!=maxFilePosFilter || newMaxLine!=maxLineFilter) {
			maxFilePosFilter = newMaxFilePos;
			maxLineFilter = newMaxLine;
			createAndSetFilter();
		}
	}

	// -----

	protected void createAndSetFilter() {
		setFilter(createFilter());
	}

	protected LogLineFilter createFilter() {
		LogLineFilter filter = null;

		if (filterThread != null) {
			final LogLineFilter fThread = new LogLineThreadFilter(filterThread);
			filter = createConjunction(filter, fThread);
		}

		if (filterThreadsActive) {
			String[] threadsArray = filterThreads.toArray(new String[0]);
			final LogLineFilter fThreads = new LogLineThreadFilter(threadsArray);
			filter = createConjunction(filter, fThreads);
		}

		if (stacktraceShowMode != StacktraceShowMode.SHOW) {
			LogLineFilter fStack = createFilter(stacktraceShowMode);
			filter = createConjunction(filter, fStack);
		}

		if (minFilePosFilter!=0L || maxFilePosFilter!=Long.MAX_VALUE) {
			LogLineFilter fPos = new LogLineFilterPos(minFilePosFilter, minLineFilter
			 , maxFilePosFilter, maxLineFilter);
			filter = createConjunction(filter, fPos);
		}

		return filter!=null ? filter : new LogLineFilterAll();
	}

	private LogLineFilter createConjunction(LogLineFilter filterOne, LogLineFilter filterTwo) {
		if (filterOne==null && filterTwo==null) {
			return null;
		} else if (filterOne!=null && filterTwo==null) {
			return filterOne;
		} else if (filterOne==null && filterTwo!=null) {
			return filterTwo;
		} else {
			return new LogLineFilterAnd(filterOne, filterTwo);
		}
	}

	private LogLineFilter createFilter(StacktraceShowMode mode) {
		switch (mode) {
			case SHOW: return new LogLineFilterAll();
			case COLLAPSE: return new LogLineFilterStackCollapse();
			case SHORT: return new LogLineFilterStackShort();
		}
		throw new IllegalStateException("unsupported: "+mode);
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

	@Override
	public void cellRendered(int width) {
		if (width > getFixedCellWidth()) {
			setFixedCellWidth(width);
		}
	}

	// ----

	private void resetView() {
		setTail(true);
		setFixedCellWidth(600);
		resetFilterSilent();
		createAndSetFilter();
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
		final boolean ctrl = e.isControlDown();
		final boolean noMod = !e.isAltDown() && !e.isAltGraphDown() && !ctrl
		 && !e.isMetaDown() && !e.isShiftDown();

		if ((noMod && (keyCode==KeyEvent.VK_UP || keyCode==KeyEvent.VK_PAGE_UP))
		 || keyCode==KeyEvent.VK_HOME) {
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

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			int sel = getSelectedIndex();
			int mods = getModel().getSize();
			if (sel>=0 && sel+1<mods) {
				if (isTail()) {
					setTail(false);
				}
			}
		}
	}

	// =======================

	private static final StacktraceShowMode[] STACKTRACE_SHOW_MODES
	 = StacktraceShowMode.values();

	private StacktraceShowMode getNext(StacktraceShowMode showMode) {
		int oldOrd = showMode.ordinal();
		int newOrd = (oldOrd+1) % STACKTRACE_SHOW_MODES.length;
		return STACKTRACE_SHOW_MODES[newOrd];
	}

	private static enum StacktraceShowMode {
		SHOW, COLLAPSE, SHORT
	};

	// =======================

	private class PrevSearch {
		private final String text;
		private final boolean isRegexp;
		private final boolean isDownSearch;

		public PrevSearch(String text, boolean isRegexp, boolean isDownSearch) {
			super();
			this.text = text;
			this.isRegexp = isRegexp;
			this.isDownSearch = isDownSearch;
		}

		private String getText() {
			return text;
		}

		private boolean isRegexp() {
			return isRegexp;
		}

		private boolean isDownSearch() {
			return isDownSearch;
		}
	}

	// =======================

	private class SearchDelegate {
		private boolean search(PrevSearch search, int fromIndexExc) {
			return search(search.getText(), search.isRegexp(), fromIndexExc, search.isDownSearch());
		}

		private boolean search(String text, boolean isRegexp
		 , int fromIndexExc, boolean searchDown) {
			if (isRegexp) {
				return searchRegexp(text, fromIndexExc, searchDown);
			} else {
				return searchText(text, fromIndexExc, searchDown);
			}
		}

		private boolean searchText(String textRaw
		 , int fromIndexExc, boolean searchDown) {
			ListModelLog model = (ListModelLog)getModel();
			String textUpper = textRaw.toUpperCase();
			if (searchDown) {
				int size = model.getSize();
				for (int index=fromIndexExc+1; index<size; index++) {
					if (checkLineAndFocus(model, index, textUpper)) {
						return true;
					}
				}
				return false;
			} else {
				for (int index=fromIndexExc-1; index>=0; index--) {
					if (checkLineAndFocus(model, index, textUpper)) {
						return true;
					}
				}
				return false;
			}
		}

		private boolean checkLineAndFocus(ListModelLog model, int index, String textUpper) {
			boolean result = checkLine(model, index, textUpper);
			if (result) {
				focusLine(index);
			}
			return result;
		}

		private boolean checkLine(ListModelLog model, int index, String textUpper) {
			LogLineFull logLineFull = model.getElementAt(index);
			String fullText = logLineFull.getFullText();
			String lineTextUpper = fullText==null ? "" : fullText.toUpperCase();
			return lineTextUpper.contains(textUpper);
		}

		private void focusLine(int index) {
			setSelectedIndex(index);
			ensureIndexIsVisible(index);
		}

		private boolean searchRegexp(String regexp
		 , int fromIndexExc, boolean searchDown) {
			ListModelLog model = (ListModelLog)getModel();
			Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
			if (searchDown) {
				int size = model.getSize();
				for (int index=fromIndexExc+1; index<size; index++) {
					if (checkLineAndFocus(model, index, pattern)) {
						return true;
					}
				}
				return false;
			} else {
				for (int index=fromIndexExc-1; index>=0; index--) {
					if (checkLineAndFocus(model, index, pattern)) {
						return true;
					}
				}
				return false;
			}
		}

		private boolean checkLineAndFocus(ListModelLog model, int index, Pattern pattern) {
			boolean result = checkLine(model, index, pattern);
			if (result) {
				focusLine(index);
			}
			return result;
		}

		private boolean checkLine(ListModelLog model, int index, Pattern pattern) {
			LogLineFull logLineFull = model.getElementAt(index);
			String fullTextRaw = logLineFull.getFullText();
			String fullText = fullTextRaw==null ? "" : fullTextRaw;
			return pattern.matcher(fullText).find();
		}

	}

	// =======================

	private class StackSearchDelegate {
		private ListModelLog model = (ListModelLog)getModel();

		private void gotoCause(int fromIndexExc, boolean dirDown) {
			int size = model.getSize();
			if (fromIndexExc<0 || fromIndexExc>=size) {
				return;
			}
			if (isStacky(fromIndexExc)
			 || (dirDown && fromIndexExc+1<size && isStacky(fromIndexExc+1))) {
				int dirDelta = dirDown ? 1 : -1;
				for (int index=fromIndexExc+dirDelta; ; index += dirDelta) {
					if (index < 0) {
						focusLine(0);
						return;
					}
					if (index >= size) {
						focusLine(size-1);
						return;
					}
					LogLineType type = model.getIndexElementAt(index).getLogLineType();
					if (type!=LogLineType.STACK_POS && type!=LogLineType.STACK_CAUSE) {
						focusLine(dirDown ? index-1 : index);
						return;
					}
					if (type == LogLineType.STACK_CAUSE) {
						focusLine(index);
						return;
					}
				}
			}
		}

		private boolean isStacky(int index) {
			LogLineType type = model.getIndexElementAt(index).getLogLineType();
			return type==LogLineType.STACK_POS || type==LogLineType.STACK_CAUSE;
		}

		private void focusLine(int index) {
			setSelectedIndex(index);
			ensureIndexIsVisible(index);
		}

	}

	// =======================

	private class SaveToFileDelegate {
		private String prevFilename;
		private final Component parent;

		private SaveToFileDelegate(Component parent) {
			this.prevFilename = Paths.get(".").toAbsolutePath().toFile().getAbsolutePath();
			this.parent = parent;
		}

		private void saveToFile() {
			String filename = pickFilename();
			if (filename == null) {
				return;
			} else {
				prevFilename = filename;
				if (!canWriteTo(filename)) {
					return;
				} else {
					ListModelLog model = (ListModelLog)getModel();
					saveToFile(model, filename);
				}
			}
		}

		private String pickFilename() {
			JFileChooser fileChooser = new JFileChooser();
			FileNameExtensionFilter extFilter
			 = new FileNameExtensionFilter("txt and log", "txt", "log");
			fileChooser.setFileFilter(extFilter);
			Path directory = getDirectory(prevFilename);
			File prevFile = directory!=null ? directory.toFile() : new File(".");
			fileChooser.setCurrentDirectory(prevFile);
			fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			int result = fileChooser.showOpenDialog(parent);
			if (result == JFileChooser.APPROVE_OPTION) {
				return fileChooser.getSelectedFile().getAbsolutePath();
			} else {
				return null;
			}
		}

		private Path getDirectory(String prev) {
			Path path = Paths.get(prev).toAbsolutePath();
			while (path != null) {
				File file = path.toFile();
				if (file.exists() && file.isDirectory()) {
					return path;
				}
				path = path.getParent();
			}
			return null;
		}

		private boolean canWriteTo(String filename) {
			File file = new File(filename);
			if (!file.exists()) {
				return true;
			}
			if (file.isDirectory()) {
				String msg = "\""+filename+"\" is a directory";
				JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				String msg = "Target file exists. Overwrite?";
				int result = JOptionPane.showConfirmDialog(parent, msg, "File exists"
				 , JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				return result == JOptionPane.YES_OPTION;
			}
		}

		private void saveToFile(ListModelLog model, String filename) {
			try {
				log.debug("writing text to \"{}\"", filename);
				saveToFileInt(model, filename);
				log.debug("file written");
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		private void saveToFileInt(ListModelLog model, String filename) throws IOException {
			try (Writer w = new FileWriter(filename)) {
				try (BufferedWriter bw = new BufferedWriter(w)) {
					int size = model.getSize();
					log.debug("will write {} lines", size);
					for (int i=0; i<size; i++) {
						LogLineFull logLineFull = model.getElementAt(i);
						String fullText = logLineFull.getFullText();
						bw.write(fullText);
						bw.newLine();
					}
				}
			}
		}
	}

	// --------------

	private static Map<String, String> parseInitFilter(String initFilter) {
		if (initFilter == null) {
			return Collections.emptyMap();
		}
		initFilter = initFilter.trim();
		if (initFilter.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<String, String> result = new HashMap<>();

		String[] split = initFilter.split(";");
		for (String str: split) {
			if (str != null) {
				str = str.trim();
				if (!str.isEmpty()) {
					int eqIndex = str.indexOf("=");
					if (eqIndex < 0) {
						log.warn("unknown opt: {}", str);
					} else {
						String key = str.substring(0, eqIndex).trim();
						String val = str.substring(eqIndex+1).trim();
						if (key.isEmpty()) {
							log.warn("key is empty in: {}", str);
						} else {
							if (result.containsKey(key)) {
								log.warn("duplicate key: {}", key);
							}
							result.put(key, val);
						}
					}
				}
			}
		}

		return result;
	}

}
