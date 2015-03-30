package pl.rychu.jew;

public class LinePosSinkNull implements LinePosSink {

	@Override
	public void put(String line, long filePos, int length) {}

	@Override
	public void reset() {}

}
