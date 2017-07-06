package pl.rychu.jew.logaccess;

public interface LinePosSink {

	boolean put(String line, long filePos, int length);

	void reset();

}
