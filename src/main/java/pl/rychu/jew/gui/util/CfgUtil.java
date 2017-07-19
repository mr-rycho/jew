package pl.rychu.jew.gui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 19.07.2017.
 */
public class CfgUtil {

	private static final Logger log = LoggerFactory.getLogger(CfgUtil.class);

	private static final String[] ENV_HOMES = new String[]{"HOME", "USERPROFILE"};

	// ----------

	public static String getFilename(String filename) {
		String homeDir = getFirstEnv(ENV_HOMES);
		log.debug("homeDir = {}", homeDir);
		return (homeDir != null ? homeDir : ".") + "/" + filename;
	}

	private static String getFirstEnv(String... envKeys) {
		return Arrays.stream(envKeys).map(System::getenv).filter(Objects::nonNull).findFirst().orElse
		 (null);
	}

	// ----------

	public static List<String> unescape(String s) {
		List<String> result = new ArrayList<>();
		StringBuilder field = new StringBuilder();
		int len = s.length();
		int index = 0;
		while (index < len) {
			char c = s.charAt(index++);
			if (c == '\\') {
				char b = s.charAt(index++);
				if (b == 'n') {
					field.append('\n');
				} else {
					field.append(b);
				}
			} else if (c == ':') {
				result.add(field.toString());
				field.setLength(0);
			} else {
				field.append(c);
			}
		}
		result.add(field.toString());
		return result;
	}

	public static String escape(Collection<Object> strs) {
		return strs.stream().map(o -> o != null ? o.toString() : "").map(CfgUtil::escape).collect
		 (Collectors.joining(":"));
	}

	private static String escape(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (c == '\n') {
				sb.append("\\n");
			} else {
				if (c == '\\' || c == ':') {
					sb.append("\\");
				}
				sb.append(c);
			}
		}
		return sb.toString();
	}

}
