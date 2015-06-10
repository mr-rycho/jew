package pl.rychu.jew.logline;



public abstract class LogLineThread extends LogLine {

	private final String threadName;

	// --------

	protected LogLineThread(final long filePos, final int length
	 , final LogLineType logLineType, final String threadName) {
		super(filePos, length, logLineType);
		this.threadName = threadName;
	}

	// --------

	@Override
	public String getThreadName() {
		return threadName;
	}

}
