package pl.rychu.jew.util;



public class StringUtil {

	private StringUtil() {}

	public static <T> String join(T[] elems) {
		if (elems==null || elems.length==0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Object obj: elems) {
			String objStr = obj==null ? "[null]" : obj.toString().trim();
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(objStr);
		}
		return sb.toString();
	}

}
