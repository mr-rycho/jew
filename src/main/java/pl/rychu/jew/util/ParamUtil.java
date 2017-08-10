package pl.rychu.jew.util;

/**
 * Created on 10.08.2017.
 */
public class ParamUtil {

	public static boolean getBooleanProperty(String key, boolean def) {
		String prop = System.getProperty(key);
		if (prop == null || prop.isEmpty()) {
			return def;
		}
		if ("true".equalsIgnoreCase(prop) || "yes".equalsIgnoreCase(prop) || "1".equals(prop)) {
			return true;
		}
		if ("false".equalsIgnoreCase(prop) || "no".equalsIgnoreCase(prop) || "0".equals(prop)) {
			return false;
		}
		return def;
	}

}
