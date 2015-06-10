package pl.rychu.jew.logline;



public class LogLineStackCause extends LogLineStack {

	protected LogLineStackCause(final long filePos, final int length
	 , final LogLineType logLineType, String threadName, String className
	 , final int stackLineTotal, final int stackLineInCause) {
		super(filePos, length, logLineType, threadName, className, stackLineTotal, stackLineInCause);
	}

	// --------

	public static LogLineStackCause create(final long filePos, final int length
	 , final String threadName, final String className
	 , final int stackLineTotal, final int stackLineInCause) {
		return new LogLineStackCause(filePos, length, LogLineType.STACK_CAUSE
		 , threadName, className, stackLineTotal, stackLineInCause);
	}

}
