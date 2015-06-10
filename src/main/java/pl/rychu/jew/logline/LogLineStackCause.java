package pl.rychu.jew.logline;



public class LogLineStackCause extends LogLineStack {

	protected LogLineStackCause(final long filePos, final int length
	 , final LogLineType logLineType, String className) {
		super(filePos, length, logLineType, className);
	}

	// --------

	public static LogLineStackCause create(final long filePos, final int length
	 , final String className) {
		return new LogLineStackCause(filePos, length, LogLineType.STACK_CAUSE, className);
	}

}
