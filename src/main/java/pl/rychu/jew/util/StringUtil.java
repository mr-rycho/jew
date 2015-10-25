package pl.rychu.jew.util;



public class StringUtil {

	private StringUtil() {}

	public static <T> String join(T[] elems) {
		return join(elems, null, null, null, null, ", ");
	}

	public static <T> String join(T[] elems, String listStart, String listEnd
	 , String elemStart, String elemEnd, String joiner) {
		StringBuilder sb = new StringBuilder();
		sb.append(listStart!=null ? listStart : "");
		if (elems != null) {
			boolean first = true;
			for (Object obj: elems) {
				String objStr = obj==null ? "[null]" : obj.toString().trim();
				if (first) first = false;
				else sb.append(joiner!=null ? joiner : "");
				sb.append(elemStart!=null ? elemStart : "");
				sb.append(objStr);
				sb.append(elemEnd!=null ? elemEnd : "");
			}
		}
		sb.append(listEnd!=null ? listEnd : "");
		return sb.toString();
	}

}
