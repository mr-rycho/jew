package pl.rychu.jew.logaccess;

public interface LinePosSink {

	void put(String line, long filePos, int length);

	void reset();

}
