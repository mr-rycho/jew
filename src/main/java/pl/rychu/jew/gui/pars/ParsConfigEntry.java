package pl.rychu.jew.gui.pars;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created on 18.07.2017.
 */
public class ParsConfigEntry {
	private final String name;
	private final String pattern;
	private final int groupTime;
	private final int groupLevel;
	private final int groupClass;
	private final int groupThread;
	private final int groupMessage;

	public ParsConfigEntry(String name, String pattern, int groupTime, int groupLevel, int
	 groupClass,
	 int groupThread, int groupMessage) {
		this.name = name;
		this.pattern = pattern;
		this.groupTime = groupTime;
		this.groupLevel = groupLevel;
		this.groupClass = groupClass;
		this.groupThread = groupThread;
		this.groupMessage = groupMessage;
	}

	public String getName() {
		return name;
	}

	public String getPattern() {
		return pattern;
	}

	public Pattern getCompiledPattern() {
		return Pattern.compile(pattern.replace("\n", ""));
	}

	public Pattern getCompiledPatternOrNull() {
		try {
			return Pattern.compile(pattern.replace("\n", ""));
		} catch (PatternSyntaxException e) {
			return null;
		}
	}

	public int getGroupTime() {
		return groupTime;
	}

	public int getGroupLevel() {
		return groupLevel;
	}

	public int getGroupClass() {
		return groupClass;
	}

	public int getGroupThread() {
		return groupThread;
	}

	public int getGroupMessage() {
		return groupMessage;
	}
}
