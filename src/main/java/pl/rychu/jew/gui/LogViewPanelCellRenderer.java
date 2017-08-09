package pl.rychu.jew.gui;

import pl.rychu.jew.gui.hi.HiConfig;
import pl.rychu.jew.gui.hi.HiConfigEntry;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class LogViewPanelCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 7313136726313412175L;

	private static final Border SEL_BORDER = new DashedToo(Color.BLACK);
	private static final Border SEL_BORDER_LITE = new DashedToo(Color.LIGHT_GRAY);

	private static final ClassVisuType[] VISU_TYPES = ClassVisuType.values();

	private ClassVisuType classVisuType = ClassVisuType.NORMAL;

	private boolean threadOffsetMode = false;
	private int parentWidth = 600;
	private int parentOffset = 0;
	private Font bookmarkFont = null;

	private final List<HiConfigEntryGui> hiConfigEntries = new ArrayList<>();

	private final FakeIcon fakeIcon = new FakeIcon();

	private String overlayToPaint = null;

	private final Map<String, Integer> threadOffsetMap = new HashMap<>();
	private BookmarkStorage.BookmarkStorageView bookmarkStorageView;

	private final List<CellRenderedListener> listeners
	 = new CopyOnWriteArrayList<>();

	private static final Random RND = new Random();

	// ----------

	void addCellRenderedListener(final CellRenderedListener listener) {
		listeners.add(listener);
	}

	// ----------

	void setHiConfig(final HiConfig hiConfig) {
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

	void setBookmarkStorageView(BookmarkStorage.BookmarkStorageView bookmarkStorageView) {
		this.bookmarkStorageView = bookmarkStorageView;
	}

	void setParentWidth(int parentWidth) {
		this.parentWidth = parentWidth;
	}

	void setParentOffset(int parentOffset) {
		this.parentOffset = parentOffset;
	}

	// ----------

	void toggleClassVisuType() {
		final int oldOrd = classVisuType!=null ? classVisuType.ordinal() : -1;
		final int newOrd = (oldOrd+1) % VISU_TYPES.length;
		setClassVisuType(VISU_TYPES[newOrd]);
	}

	private void setClassVisuType(final ClassVisuType classVisuType) {
		this.classVisuType = classVisuType;
	}

	// ----------

	void toggleThreadOffsetMode() {
		setThreadOffsetMode(!threadOffsetMode);
	}

	private void setThreadOffsetMode(boolean mode) {
		this.threadOffsetMode = mode;
	}

	// ----------

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (overlayToPaint != null) {
			Font oldFont = g.getFont();
			if (bookmarkFont == null) {
				bookmarkFont = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() * 3 / 4);
			}
			g.setFont(bookmarkFont);
			int stringWidth = g.getFontMetrics().stringWidth(overlayToPaint);
			int x = parentWidth + parentOffset - 3 - 6 - stringWidth;
			Color oldColor = g.getColor();
			g.setColor(Color.GREEN);
			g.fillRect(x, 0, stringWidth + 6, 14);
			g.setColor(Color.BLACK);
			g.drawRect(x, 0, stringWidth + 5, 13);
			g.drawString(overlayToPaint, x + 3, 12);
			g.setColor(oldColor);
			g.setFont(oldFont);
		}
	}

	@Override
  public Component getListCellRendererComponent(final JList<?> list
   , final Object value, final int index, final boolean isSelected
   , final boolean cellHasFocus) {
		final LogLineFull logLineFull = (LogLineFull)value;
		final String fullText = getFullString(logLineFull);
		LogLine logLine = logLineFull.getLogLine();
		String threadName = logLine.getThreadName();
		int hash = getThreadHash(threadName);
		int offset = threadOffsetMode ? hash & 0xff : -1;
		final String logLineStr = getRenderedString(logLineFull);
		List<Integer> bookmarks = bookmarkStorageView.getOffsetToBookmark().get(logLine.getFilePos());
		overlayToPaint = bookmarks == null || bookmarks.isEmpty() ? null : bookmarks.stream().map
		 (Objects::toString).collect(Collectors.joining(", "));
		return getListCellRendererComponentSuper(list, fullText, logLineStr, isSelected, cellHasFocus,
		 offset);
	}

	private Component getListCellRendererComponentSuper(JList<?> list, String fullText,
	 String displayedText, boolean isSelected, boolean cellHasFocus, int xOffset) {
		setComponentOrientation(list.getComponentOrientation());

		Color bg = null;
		Color fg = null;

		for (final HiConfigEntryGui entry : hiConfigEntries) {
			final Pattern pattern = entry.getPattern();
			final Matcher matcher = pattern.matcher(fullText);
			if (matcher.find()) {
				bg = entry.getColorB();
				fg = entry.getColorF();
				break;
			}
		}
		setBackground(bg == null ? list.getBackground() : bg);
		setForeground(fg == null ? list.getForeground() : fg);

		setText(displayedText);
		if (xOffset<0 && getIcon()!=null) {
			setIcon(null);
		}
		if (xOffset>=0) {
			fakeIcon.setIconWidth(xOffset);
			if (getIcon() == null) {
				setIcon(fakeIcon);
			}
		}

		setEnabled(list.isEnabled());
		setFont(list.getFont());

		Border border = cellHasFocus ? SEL_BORDER : (isSelected ? SEL_BORDER_LITE : noFocusBorder);
		setBorder(border);

		if (!listeners.isEmpty()) {
			int width = getPreferredSize().width;
			for (CellRenderedListener lsn: listeners) {
				lsn.cellRendered(width);
			}
		}

		return this;
	}

	private String getFullString(final LogLineFull logLineFull) {
		return logLineFull!=null ? logLineFull.getFullText() : "~";
	}

	private String getRenderedString(final LogLineFull logLineFull) {
		if (logLineFull == null) {
			return "~";
		} else {
			String currentText = logLineFull.getFullText();
			currentText = replaceClass(logLineFull, currentText);
			currentText = replaceTab(currentText);
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

	private String replaceTab(String src) {
		if (src!=null && src.contains("\t")) {
			return src.replace("\t", "    ");
		} else {
			return src;
		}
	}

	private int getThreadHash(String threadName) {
		if (!threadOffsetMap.containsKey(threadName)) {
			threadOffsetMap.put(threadName, RND.nextInt());
		}
		return threadOffsetMap.get(threadName);
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

		HiConfigEntryGui(final Pattern pattern, final Color colorB, final Color colorF) {
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

	private static class DashedToo extends LineBorder {
		private static final long serialVersionUID = 2816580155760767943L;

		DashedToo(Color color)  {
			super(color, 1);
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Color oldColor = g.getColor();

			g.setColor(lineColor);
			for (int vx = x; vx < x+width; vx++) {
				int off = vx & 1;
				g.fillRect(vx, y+off, 1, 1);
				g.fillRect(vx, y+height-1-off, 1, 1);
			}
			g.setColor(oldColor);
		}
	}

	// ==================

	private enum ClassVisuType {
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

	private static class FakeIcon implements Icon {

		private int width = 0;

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {}

		@Override
		public int getIconWidth() {
			return width;
		}

		void setIconWidth(int width) {
			this.width = width;
		}

		@Override
		public int getIconHeight() {
			return 1;
		}
	}

}
