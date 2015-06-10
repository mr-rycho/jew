package pl.rychu.jew;


public class LogLineStd extends LogLineClass {

	private final long timestamp;
	private final String level;
	private final String threadName;

	// --------

	protected LogLineStd(final long filePos, final int length
	 , final LogLineType logLineType, final long timestamp, final String level
	 , final String className, final String threadName) {
		super(filePos, length, logLineType, className);
		this.timestamp = timestamp;
		this.level = level;
		this.threadName = threadName;
	}

	// --------

	public static LogLineStd create(final long filePos, final int length
	 , final long timestamp, final String level, final String className
	 , final String threadName) {
		return new LogLineStd(filePos, length, LogLineType.STANDARD, timestamp
		 , level, className, threadName);
	}

	// --------

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String getLevel() {
		return level;
	}

	@Override
	public String getThreadName() {
		return threadName;
	}

}
