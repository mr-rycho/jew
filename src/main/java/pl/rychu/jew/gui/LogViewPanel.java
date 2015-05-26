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

import pl.rychu.jew.LogLineFull;



public class LogViewPanel extends JList<LogLineFull> implements CyclicModelListener
 , KeyListener {

	private static final long serialVersionUID = -6731368974272464443L;

	private static final Logger log = LoggerFactory.getLogger(LogViewPanel.class);

	private static final String ACTION_KEY_TOGGLE_TAIL = "jew.toggleTail";

	private boolean tail;

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
	}

	private static void createKeyBindings(final LogViewPanel logViewPanel) {
		final InputMap inputMap = new InputMap();
		final InputMap oldInputMap = logViewPanel.getInputMap();
		inputMap.setParent(oldInputMap);
		logViewPanel.setInputMap(WHEN_FOCUSED, inputMap);

		inputMap.put(KeyStroke.getKeyStroke('`'), ACTION_KEY_TOGGLE_TAIL);
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

	// -----

	private int prevSize = 0;

	@Override
	public void linesAddedStart(int numberOfLinesAdded, final long total) {
		if (tail) {
			tail((int)total);
		} else {
			scrollForward(numberOfLinesAdded);
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
	public void listReset() {
		ensureIndexIsVisible(0);
		prevSize = 0;
	}

	@Override
	public void sourceChanged(long totalSourceLines) {}

	// ----

	private void scrollForward(final int linesToScroll) {
		final int lastVisibleIndex = getLastWholeVisibleIndex();
		final int index
		 = lastVisibleIndex >= 0
		 ? lastVisibleIndex + linesToScroll
		 : linesToScroll - 1;

		ensureIndexIsVisible(index);

		final int newFirstVisibleIndex = getFirstVisibleIndex();
		final int newLastVisibleIndex = getLastWholeVisibleIndex();
		log.trace("new visible indexes: {}-{}", newFirstVisibleIndex
		 , newLastVisibleIndex);
		if (newFirstVisibleIndex<0 || newLastVisibleIndex<0
		 || index<newFirstVisibleIndex || index>newLastVisibleIndex) {
			log.trace("trying to scroll again because {} != ({}, {})", index
			 , newFirstVisibleIndex, newLastVisibleIndex);
			ensureIndexIsVisible(index);
			final int newestFirstVisibleIndex = getFirstVisibleIndex();
			final int newestLastVisibleIndex = getLastWholeVisibleIndex();
			log.trace("newest visible indexes: {}-{}", newestFirstVisibleIndex
			 , newestLastVisibleIndex);
		}
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
