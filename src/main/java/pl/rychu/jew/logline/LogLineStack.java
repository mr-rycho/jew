package pl.rychu.jew.logline;



public class LogLineStack extends LogLineThreadClass {

	protected LogLineStack(final long filePos, final int length
	 , final LogLineType logLineType, String threadName, String className) {
		super(filePos, length, logLineType, threadName, className);
	}

	// --------

	public static LogLineStack create(final long filePos, final int length
	 , final String threadName, final String className) {
		return new LogLineStack(filePos, length, LogLineType.STACK_POS, threadName, className);
	}

}
