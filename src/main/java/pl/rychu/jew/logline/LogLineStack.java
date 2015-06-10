package pl.rychu.jew.logline;



public class LogLineStack extends LogLineThreadClass {

	private final int stackLineTotal;
	private final int stackLineInCause;

	protected LogLineStack(final long filePos, final int length
	 , final LogLineType logLineType, String threadName, String className
	 , final int stackLineTotal, final int stackLineInCause) {
		super(filePos, length, logLineType, threadName, className);
		this.stackLineTotal = stackLineTotal;
		this.stackLineInCause = stackLineInCause;
	}

	// --------

	public static LogLineStack create(final long filePos, final int length
	 , final String threadName, final String className
	 , final int stackLineTotal, final int stackLineInCause) {
		return new LogLineStack(filePos, length, LogLineType.STACK_POS
		 , threadName, className, stackLineTotal, stackLineInCause);
	}

	// --------

	public int getStackLineTotal() {
		return stackLineTotal;
	}

	public int getStackLineInCause() {
		return stackLineInCause;
	}

}
