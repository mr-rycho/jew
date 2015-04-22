package pl.rychu.jew;

public interface LogAccess {

	public int getVersion();

	public long size(int version);

	public LogLine get(long pos, int version);

	public LogLineFull getFull(long pos, int version);

}
