package pl.rychu.jew.gui;

public interface CyclicModelListener {

	void linesAddedStart(int numberOfLinesAdded, long totalLines);

	void linesAddedEnd(int numberOfLinesAdded, long totalLines);

	void listReset(boolean sourceReset);

	void sourceChanged(long totalSourceLines);

}
