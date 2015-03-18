package pl.rychu.jew;



public class LogLineFull  {

	private final LogLine logLine;
	private final String fullText;


	public LogLineFull(final LogLine logLine, final String fullText) {
		this.logLine = logLine;
		this.fullText = fullText;
	}


	public LogLine getLogLine() {
		return logLine;
	}

	public String getFullText() {
		return fullText;
	}

}
