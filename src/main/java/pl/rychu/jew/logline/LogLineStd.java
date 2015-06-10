package pl.rychu.jew.logline;


public class LogLineStd extends LogLineThreadClass {

	private final long timestamp;
	private final String level;

	// --------

	protected LogLineStd(final long filePos, final int length
	 , final LogLineType logLineType, final long timestamp, final String level
	 , final String className, final String threadName) {
		super(filePos, length, logLineType, threadName, className);
		this.timestamp = timestamp;
		this.level = level;
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

}
