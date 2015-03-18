package pl.rychu.jew.linedec;

import pl.rychu.jew.LogLine;



public interface LineDecoder {

	/**
	 * @param filePos
	 * @param line
	 * @return {@link LogLine} or {@code null} when not decoded
	 */
	LogLine decode(long filePos, String line, int length);

}
