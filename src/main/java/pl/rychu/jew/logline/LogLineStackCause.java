package pl.rychu.jew.logline;



public class LogLineStackCause extends LogLineStack {

	protected LogLineStackCause(final long filePos, final int length
	 , final LogLineType logLineType, String threadName, String className) {
		super(filePos, length, logLineType, threadName, className);
	}

	// --------

	public static LogLineStackCause create(final long filePos, final int length
	 , final String threadName, final String className) {
		return new LogLineStackCause(filePos, length, LogLineType.STACK_CAUSE, threadName, className);
	}

}
