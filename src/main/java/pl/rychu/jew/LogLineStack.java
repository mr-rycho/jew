package pl.rychu.jew;



public class LogLineStack extends LogLineClass {

	protected LogLineStack(final long filePos, final int length
	 , final LogLineType logLineType, String className) {
		super(filePos, length, logLineType, className);
	}

	// --------

	public static LogLineStack create(final long filePos, final int length
	 , final String className) {
		return new LogLineStack(filePos, length, LogLineType.STACK_POS, className);
	}

}
