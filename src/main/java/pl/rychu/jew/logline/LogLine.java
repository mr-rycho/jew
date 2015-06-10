package pl.rychu.jew.logline;

public class LogLine {

	private final long filePos;
	private final int length;
	private final LogLineType logLineType;

	// --------

	protected LogLine(final long filePos, final int length
	 , final LogLineType logLineType) {
		super();
		this.filePos = filePos;
		this.length = length;
		this.logLineType = logLineType;
	}

	// --------

	public long getFilePos() {
		return filePos;
	}

	public int getLength() {
		return length;
	}

	public LogLineType getLogLineType() {
		return logLineType;
	}

	// --------

	public String getClassName() {
		return null; // N/A
	}

	public long getTimestamp() {
		return Long.MIN_VALUE; // N/A
	}

	public String getLevel() {
		return null; // N/A
	}

	public String getThreadName() {
		return null; // N/A
	}

	// ==============

	public static enum LogLineType {
		STANDARD, STACK_POS, STACK_CAUSE, TEXT
	}

}
