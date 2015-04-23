package pl.rychu.jew;

import pl.rychu.jew.gl.BadVersionException;

public interface LogAccess {

	public int getVersion();

	public long size(int version) throws BadVersionException;

	public LogLine get(long pos, int version) throws BadVersionException;

	public LogLineFull getFull(long pos, int version) throws BadVersionException;

}
