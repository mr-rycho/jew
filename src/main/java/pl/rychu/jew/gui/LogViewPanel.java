package pl.rychu.jew.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.Position.Bias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.LogLine;
import pl.rychu.jew.LogLineFull;
import pl.rychu.jew.filter.LogLineFilter;
import pl.rychu.jew.filter.LogLineFilterAll;
import pl.rychu.jew.filter.LogLineThreadFilter;
import pl.rychu.jew.gui.hi.HiConfig;
import pl.rychu.jew.gui.hi.HiConfigEntry;
import pl.rychu.jew.gui.hi.HiDialog;



public class LogViewPanel extends JList<LogLineFull> implements CyclicModelListener
 , KeyListener {

	private static final long serialVersionUID = -6731368974272464443L;

	private static final Logger log = LoggerFactory.getLogger(LogViewPanel.class);

	private static final String ACTION_KEY_TOGGLE_TAIL = "jew.toggleTail";
	private static final String ACTION_KEY_FILTER_TOGGLE_THREAD = "jew.flt.thread";

	private static final String ACTION_KEY_RNDR_TOGGLE_CLASS = "jew.rndr.toggleClass";

	private static final String ACTION_KEY_HI_DIALOG = "jew.hi.dialog";

	private boolean tail;

	private String filterThread;

	private HiConfig hiConfig;

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

		actionMap.put(ACTION_KEY_RNDR_TOGGLE_CLASS, new AbstractAction(ACTION_KEY_RNDR_TOGGLE_CLASS) {
			private static final long serialVersionUID = 3385146847577067253L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Object sourceObj = e.getSource();
				if (sourceObj instanceof LogViewPanel) {
					final LogViewPanel logViewPanel = (LogViewPanel)sourceObj;
					CellRenderer cellRenderer = (CellRenderer)logViewPanel.getCellRenderer();
					cellRenderer.toggleClassVisuType();
					logViewPanel.repaint();
				}
			}
		});

		actionMap.put(ACTION_KEY_HI_DIALOG, new AbstractAction(ACTION_KEY_HI_DIALOG) {
			private static final long serialVersionUID = -8346845550957257182L;
			@Override
			public void actionPerformed(final ActionEvent e) {
				HiDialog hiDialog
				 = new HiDialog((JFrame)logViewPanel.getTopLevelAncestor(), logViewPanel.hiConfig);
				// execution continues here after closing the dialog
				HiConfig hiConfig = hiDialog.get();
				hiDialog.dispose();
				if (hiConfig != null) {
					logViewPanel.setHiConfig(hiConfig);
					logViewPanel.repaint();
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

	public void setHiConfig(final HiConfig hiConfig) {
		this.hiConfig = hiConfig;
		((CellRenderer)getCellRenderer()).setHiConfig(hiConfig);
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

	private static enum ClassVisuType {
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

	// ==================

	private static class CellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 7313136726313412175L;

		private static final Border SEL_BORDER = new LineBorder(Color.BLACK, 1);

		private static final ClassVisuType[] VISU_TYPES = ClassVisuType.values();

		private ClassVisuType classVisuType = ClassVisuType.NORMAL;

		private final List<HiConfigEntryGui> hiConfigEntries = new ArrayList<>();

		// ----------

		public void setHiConfig(final HiConfig hiConfig) {
			hiConfigEntries.clear();
			final int len = hiConfig.size();
			for (int i=0; i<len; i++) {
				final HiConfigEntry entry = hiConfig.get(i);
				final String regexpStr = entry.getRegexp();
				try {
					final Pattern regexp = Pattern.compile(regexpStr);
					final Color colorB = new Color(entry.getColorB());
					final Color colorF = new Color(entry.getColorF());
					hiConfigEntries.add(new HiConfigEntryGui(regexp, colorB, colorF));
				} catch (Exception e) {
					// TODO log? msgBox?
				}
			}
		}

		// ----------

		public void toggleClassVisuType() {
			final int oldOrd = classVisuType!=null ? classVisuType.ordinal() : -1;
			final int newOrd = (oldOrd+1) % VISU_TYPES.length;
			setClassVisuType(VISU_TYPES[newOrd]);
		}

		public void setClassVisuType(final ClassVisuType classVisuType) {
			this.classVisuType = classVisuType;
		}

		@Override
    public Component getListCellRendererComponent(final JList<?> list
     , final Object value, final int index, final boolean isSelected
     , final boolean cellHasFocus) {
			final LogLineFull logLineFull = (LogLineFull)value;
			final String logLineStr = getRenderedString(logLineFull);
			return getListCellRendererComponentSuper(list, logLineStr, index
			 , isSelected, cellHasFocus);
		}

		private Component getListCellRendererComponentSuper(final JList<?> list
		 , final Object value, final int index, final boolean isSelected
		 , final boolean cellHasFocus) {
			setComponentOrientation(list.getComponentOrientation());

			final String text = (value == null) ? "" : value.toString();

			Color bg = null;
			Color fg = null;

			final int len = hiConfigEntries.size();
			for (int i=0; i<len; i++) {
				final HiConfigEntryGui entry = hiConfigEntries.get(i);
				final Pattern pattern = entry.getPattern();
				final Matcher matcher = pattern.matcher(text);
				if (matcher.find()) {
					bg = entry.getColorB();
					fg = entry.getColorF();
					break;
				}
			}
			setBackground(bg == null ? list.getBackground() : bg);
			setForeground(fg == null ? list.getForeground() : fg);

			setIcon(null);
			setText(text);

			setEnabled(list.isEnabled());
			setFont(list.getFont());

			Border border = noFocusBorder;
			if (cellHasFocus && isSelected) {
				border = SEL_BORDER;
			}
			setBorder(border);

			return this;
		}

		private String getRenderedString(final LogLineFull logLineFull) {
			if (logLineFull == null) {
				return "~";
			} else {
				String currentText = logLineFull.getFullText();
				currentText = replaceClass(logLineFull, currentText);
				return currentText;
			}
		}

		private String replaceClass(final LogLineFull logLineFull, final String currentText) {
			if (!classVisuType.replacesClass()) {
				return currentText;
			} else {
				final String className = logLineFull.getLogLine().getClassName();
				if (className==null || className.isEmpty()) {
					return currentText;
				} else {
					final String findStr = " ["+className+"]";
					final int findStrIndex = currentText.indexOf(findStr);
					if (findStrIndex < 0) {
						return currentText;
					} else {
						final String replacement = classVisuType.getReplacement(className);
						final int findStrLen = findStr.length();
						return currentText.substring(0, findStrIndex)+" ["+replacement+"]"
						 +currentText.substring(findStrIndex+findStrLen);
					}
				}
			}
		}

		@Override
		public boolean isOpaque() {
			return true;
		}

		// ====================

		private static class HiConfigEntryGui {
			private final Pattern pattern;
			private final Color colorB;
			private final Color colorF;

			public HiConfigEntryGui(final Pattern pattern
			 , final Color colorB, final Color colorF) {
				super();
				this.pattern = pattern;
				this.colorB = colorB;
				this.colorF = colorF;
			}

			private Pattern getPattern() {
				return pattern;
			}
			private Color getColorB() {
				return colorB;
			}
			private Color getColorF() {
				return colorF;
			}
		}

	}

}
