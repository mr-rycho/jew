package pl.rychu.jew.conf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



public enum LoggerType {

	WILDFLY_STD("wild", "wf")
	, GLASSFISH_STD("glass", "gf")
	, APP;

	// ---

	private final Set<String> aliases;

	// ---

	private LoggerType(String... aliases) {
		List<String> list = new ArrayList<>(Arrays.asList(aliases));
		list.add(name());
		Set<String> set = list.stream().map(String::toLowerCase)
		 .collect(Collectors.toSet());
		this.aliases = Collections.unmodifiableSet(set);
	}

	public static LoggerType get(String alias) {
		String aliasLow = alias.toLowerCase();
		for (LoggerType loggerType: values()) {
			if (loggerType.aliases.contains(aliasLow)) {
				return loggerType;
			}
		}
		throw new IllegalArgumentException("no logger with alias "+alias);
	}

	public static String getTypes() {
		StringBuilder sb = new StringBuilder();
		for (LoggerType loggerType: values()) {
			for (String alias: loggerType.aliases) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(alias);
			}
		}
		return sb.toString();
	}

}
