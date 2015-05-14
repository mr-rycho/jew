package pl.rychu.jew;

import pl.rychu.jew.gl.BadVersionException;

public interface LogAccess {

	int getVersion();

	long sizeF(int version) throws BadVersionException;

	long sizeB(int version) throws BadVersionException;

	LogLine get(long pos, int version) throws BadVersionException;

	LogLineFull getFull(long pos, int version) throws BadVersionException;

	void dispose();

	long getRootIndex(long pos, int version) throws BadVersionException;

}
