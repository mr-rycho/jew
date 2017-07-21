package pl.rychu.jew.logaccess;

import pl.rychu.jew.gl.BadVersionException;
import pl.rychu.jew.linedec.LineDecoderCfg;
import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineFull;

public interface LogAccess {

	int getVersion();

	long size(int version) throws BadVersionException;

	LogLine get(long pos, int version) throws BadVersionException;

	LogLineFull getFull(long pos, int version) throws BadVersionException;

	void reconfig(LineDecoderCfg lineDecoderCfg);

}
