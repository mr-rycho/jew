package pl.rychu.jew.linedec;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineStack;
import pl.rychu.jew.logline.LogLine.LogLineType;

public class LineDecoderUtil {

	private LineDecoderUtil() {}

	public static LogLineStack getLogLineStack(LogLine prevLine) {
		if (prevLine!=null) {
			LogLineType type = prevLine.getLogLineType();
			if (type==LogLineType.STACK_POS || type==LogLineType.STACK_CAUSE) {
				return (LogLineStack)prevLine;
			}
		}
		return null;
	}

}
