package pl.rychu.jew;

public interface LinePosSink {

	void put(String line, long filePos, int length);

	void reset();

}
