package pl.rychu.jew.logline;



public class LogLineText extends LogLineThread {

	protected LogLineText(final long filePos, final int length
	 , final LogLineType logLineType, String threadName) {
		super(filePos, length, logLineType, threadName);
	}

	// --------

	public static LogLineText create(final long filePos, final int length, String threadName) {
		return new LogLineText(filePos, length, LogLineType.TEXT, threadName);
	}

}
