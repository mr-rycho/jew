package pl.rychu.jew.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import pl.rychu.jew.gui.hi.HiConfig;
import pl.rychu.jew.gui.hi.HiConfigEntry;
import pl.rychu.jew.logline.LogLineFull;

public class LogViewPanelCellRenderer extends DefaultListCellRenderer {
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
			final LogViewPanelCellRenderer.HiConfigEntryGui entry = hiConfigEntries.get(i);
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

}
