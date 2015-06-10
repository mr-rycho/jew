package pl.rychu.jew.logline;



public abstract class LogLineClass extends LogLine {

	private final String className;

	// --------

	protected LogLineClass(final long filePos, final int length
	 , final LogLineType logLineType, final String className) {
		super(filePos, length, logLineType);
		this.className = className;
	}

	// --------

	@Override
	public String getClassName() {
		return className;
	}

}
