package pl.rychu.jew;

public class LogLine {

	private final long filePos;
	private final int length;
	private final LogLineType logLineType;
	private final long timestamp;
	private final String level;
	private final String className;
	private final String threadName;


	private LogLine(final long filePos, final int length
	 , final LogLineType logLineType, final long timestamp, final String level
	 , final String className, final String threadName) {
		super();
		this.filePos = filePos;
		this.length = length;
		this.logLineType = logLineType;
		this.timestamp = timestamp;
		this.level = level;
		this.className = className;
		this.threadName = threadName;
	}

	public static LogLine createStandard(final long filePos, final int length
	 , final long timestamp, final String level, final String className
	 , final String threadName) {
		return new LogLine(filePos, length, LogLineType.STANDARD, timestamp
		 , level, className, threadName);
	}

	public static LogLine createStack(final long filePos, final int length
	 , final String className)
	{
		return new LogLine(filePos, length, LogLineType.STACK_POS, 0L, null
		 , className, null);
	}

	public static LogLine createStackCause(final long filePos, final int length
	 , final String className)
	{
		return new LogLine(filePos, length, LogLineType.STACK_CAUSE, 0L, null
		 , className, null);
	}

	public static LogLine createText(final long filePos, final int length) {
		return new LogLine(filePos, length, LogLineType.TEXT, 0L, null, null, null);
	}


	public long getFilePos() {
		return filePos;
	}

	public int getLength() {
		return length;
	}

	public LogLineType getLogLineType() {
		return logLineType;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getLevel() {
		return level;
	}

	public String getClassName() {
		return className;
	}

	public String getThreadName() {
		return threadName;
	}

	// ==============

	public static enum LogLineType {
		STANDARD, STACK_POS, STACK_CAUSE, TEXT
	}

}
