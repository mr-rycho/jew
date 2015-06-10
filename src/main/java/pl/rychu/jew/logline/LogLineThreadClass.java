package pl.rychu.jew.logline;



public abstract class LogLineThreadClass extends LogLineThread {

	private final String className;

	// --------

	protected LogLineThreadClass(final long filePos, final int length
	 , final LogLineType logLineType, final String threadName, final String className) {
		super(filePos, length, logLineType, threadName);
		this.className = className;
	}

	// --------

	@Override
	public String getClassName() {
		return className;
	}

}
