package pl.rychu.jew.logaccess;

public class LinePosSinkNull implements LinePosSink {

	@Override
	public boolean put(String line, long filePos, int length) {
		return false;
	}

	@Override
	public void reset() {
	}

}
