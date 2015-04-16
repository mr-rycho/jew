package pl.rychu.jew;

public interface LogAccess {

	public void addLogListener(LogListener l);

	public void removeLogListener(LogListener l);

	public long size();

	public LogLine get(long pos);

	public LogLineFull getFull(long pos);

}
