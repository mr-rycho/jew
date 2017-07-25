package pl.rychu.jew.linedec;

import pl.rychu.jew.logline.LogLine;
import pl.rychu.jew.logline.LogLineStd;

import java.util.regex.Matcher;

/**
 * Created on 14.07.2017.
 */
public class LineDecoderRegex implements LineDecoder {

	private LineDecoderCfg lineDecoderCfg;

	public LineDecoderRegex(LineDecoderCfg lineDecoderCfg) {
		this.lineDecoderCfg = lineDecoderCfg;
	}

	public void reconfig(LineDecoderCfg lineDecoderCfg) {
		this.lineDecoderCfg = lineDecoderCfg;
	}

	@Override
	public LogLine decode(long filePos, String line, int length, LogLine prevLine) {
		if (line == null || line.isEmpty()) {
			return null;
		}

		if (lineDecoderCfg == null || lineDecoderCfg.getPattern() == null) {
			return null;
		}

		Matcher matcher = lineDecoderCfg.getPattern().matcher(line);

		if (!matcher.matches()) {
			return null;
		}

		String levelRaw = matcher.group(lineDecoderCfg.getGroupLevel());
		String classnameRaw = matcher.group(lineDecoderCfg.getGroupClass());
		String threadRaw = matcher.group(lineDecoderCfg.getGroupThread());

		String level = LogElemsCache.getOrPutLevel(levelRaw);
		String classname = LogElemsCache.getOrPutLogger(classnameRaw);
		String threadName = LogElemsCache.getOrPutThread(threadRaw);
		return LogLineStd.create(filePos, length, 0L, level, classname, threadName);
	}

}
