package pl.rychu.jew;

public class LogLine {

	private final long filePos;
	private final LogLineType logLineType;
	private final long timestamp;
	private final String level;
	private final String className;
	private final String threadName;


	private LogLine(final long filePos, final LogLineType logLineType
	 , final long timestamp, final String level, final String className
	 , final String threadName) {
		super();
		this.filePos = filePos;
		this.logLineType = logLineType;
		this.timestamp = timestamp;
		this.level = level;
		this.className = className;
		this.threadName = threadName;
	}

	public static LogLine createStandard(final long filePos
	 , final long timestamp, final String level, final String className
	 , final String threadName) {
		return new LogLine(filePos, LogLineType.STANDARD, timestamp
		 , level, className, threadName);
	}

	public static LogLine createStack(final long filePos, final String className)
	{
		return new LogLine(filePos, LogLineType.STACK_POS, 0L, null, className, null);
	}

	public static LogLine createStackCause(final long filePos, final String className)
	{
		return new LogLine(filePos, LogLineType.STACK_CAUSE, 0L, null, className, null);
	}

	public static LogLine createText(final long filePos) {
		return new LogLine(filePos, LogLineType.TEXT, 0L, null, null, null);
	}


	protected long getFilePos() {
		return filePos;
	}

	protected LogLineType getLogLineType() {
		return logLineType;
	}

	protected long getTimestamp() {
		return timestamp;
	}

	protected String getLevel() {
		return level;
	}

	protected String getClassName() {
		return className;
	}

	protected String getThreadName() {
		return threadName;
	}

	// ==============

	public static enum LogLineType {
		STANDARD, STACK_POS, STACK_CAUSE, TEXT
	}

}
