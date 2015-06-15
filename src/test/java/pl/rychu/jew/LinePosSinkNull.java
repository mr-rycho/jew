package pl.rychu.jew;

import pl.rychu.jew.logaccess.LinePosSink;

public class LinePosSinkNull implements LinePosSink {

	@Override
	public void put(String line, long filePos, int length) {}

	@Override
	public void reset() {}

}
