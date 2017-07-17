package pl.rychu.jew.linedec;

import java.util.regex.Pattern;

/**
 * Created on 14.07.2017.
 */
public class LineDecoderCfg {
	private final Pattern pattern;
	private final int groupTime;
	private final int groupLevel;
	private final int groupClass;
	private final int groupThread;
	private final int groupMessage;

	public LineDecoderCfg(Pattern pattern, int groupTime, int groupLevel, int groupClass,
	 int groupThread, int groupMessage) {
		this.pattern = pattern;
		this.groupTime = groupTime;
		this.groupLevel = groupLevel;
		this.groupClass = groupClass;
		this.groupThread = groupThread;
		this.groupMessage = groupMessage;
	}

	public Pattern getPattern() {
		return pattern;
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
