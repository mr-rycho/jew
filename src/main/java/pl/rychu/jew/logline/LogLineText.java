package pl.rychu.jew.logline;



public class LogLineText extends LogLine {

	protected LogLineText(final long filePos, final int length
	 , final LogLineType logLineType) {
		super(filePos, length, logLineType);
	}

	// --------

	public static LogLineText create(final long filePos, final int length) {
		return new LogLineText(filePos, length, LogLineType.TEXT);
	}

}
