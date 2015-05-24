package pl.rychu.jew.gui;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.rychu.jew.LogLineFull;



public class LogViewPanel extends JList<LogLineFull> implements CyclicModelListener {

	private static final long serialVersionUID = -6731368974272464443L;

	private static final Logger log = LoggerFactory.getLogger(LogViewPanel.class);

	// ---------

	public static LogViewPanel create(final ListModelLog model) {
		final LogViewPanel result = new LogViewPanel();
		result.setFixedCellWidth(600);
		result.setFixedCellHeight(14);
		result.setCellRenderer(new CellRenderer());
		result.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		result.setModel(model);

		return result;
	}

	// ---------

	private int prevSize = 0;

	@Override
	public void linesAddedStart(int numberOfLinesAdded, final long total) {
		scrollForward(numberOfLinesAdded);
		prevSize = (int)total;
	}

	@Override
	public void linesAddedEnd(int numberOfLinesAdded, final long total) {
		if (prevSize == 0) {
			setSelectedIndex(0);
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
