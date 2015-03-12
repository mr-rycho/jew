package pl.rychu.jew.linedec;

import java.util.HashMap;
import java.util.Map;


// TODO make it a guice bean
// TODO make it thread safe
public class LogElemsCache {

	private static final Map<String, String> levels = new HashMap<>();

	private static final Map<String, String> loggers = new HashMap<>();

	private static final Map<String, String> threads = new HashMap<>();

	// ----------

	public static int getThreadCount() {
		return threads.size();
	}

	// -----------

	public static String getOrPutLevel(final String level) {
		return getOrPutValue(level, levels);
	}

	public static String getOrPutLogger(final String logger) {
		return getOrPutValue(logger, loggers);
	}

	public static String getOrPutThread(final String thread) {
		return getOrPutValue(thread, threads);
	}

	// -----------

	private static String getOrPutValue(final String value
	 , final Map<String, String> values) {
		if (values.containsKey(value)) {
			return values.get(value);
		} else {
			final String keyValue = new String(value);
			values.put(keyValue, keyValue);
			return keyValue;
		}
	}


}
