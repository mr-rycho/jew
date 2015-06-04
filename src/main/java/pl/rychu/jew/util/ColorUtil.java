package pl.rychu.jew.util;

public class ColorUtil {

	public static int getColorSafe(String str) {
		try {
			return getColor(str);
		} catch (RuntimeException e) {
			return 0xffffff;
		}
	}

	public static int getColor(final String str) {
		if (str.length() == 3) {
			return getColor3(str);
		} else if (str.length() == 6) {
				return getColor6(str);
		} else {
			throw new IllegalStateException("expecting color, got: "+str);
		}
	}

	private static int getColor3(final String str) {
		if (str.length() != 3) {
			throw new IllegalStateException("expecting 3 hex digits, got: "+str);
		}
		final char c1 = str.charAt(0);
		final char c2 = str.charAt(1);
		final char c3 = str.charAt(2);
		return getColor6(""+c1+c1+c2+c2+c3+c3);
	}

	private static int getColor6(final String str) {
		if (str.length() != 6) {
			throw new IllegalStateException("expecting 6 hex digits, got: "+str);
		}
		return readHex(str);
	}

	private static int readHex(final String str) {
		int num = 0;
		final int len = str.length();
		for (int i=0; i<len; i++) {
			final char c = str.charAt(i);
			num = (num<<4) | readHexDigit(c);
		}
		return num;
	}

	private static int readHexDigit(final char c) {
		if (c>='0' && c<='9') {
			return (int)(c - '0');
		} else if (c>='a' && c<='f') {
			return 10 + (int)(c - 'a');
		} else if (c>='A' && c<='F') {
			return 10 + (int)(c - 'A');
		} else {
			throw new IllegalStateException("incorrect hex digit: "+c);
		}
	}

	public static String toCssColor(int color) {
		return isCompressible(color) ? getColor3(color) : getColor6(color);
	}

	private static boolean isCompressible(int color) {
		return (color & 0x555) == ((color & 0xaaa) >> 4);
	}

	private static String getColor3(int color) {
		StringBuilder sb = new StringBuilder(3);
		sb.append(toHexDigit(0x0f & getRed(color)));
		sb.append(toHexDigit(0x0f & getGreen(color)));
		sb.append(toHexDigit(0x0f & getBlue(color)));
		return sb.toString();
	}

	private static String getColor6(int color) {
		return toHexByte(getRed(color))+toHexByte(getGreen(color))+toHexByte(getBlue(color));
	}

	private static int getRed(int color) {
		return 0xff & (color >> 16);
	}

	private static int getGreen(int color) {
		return 0xff & (color >> 8);
	}

	private static int getBlue(int color) {
		return 0xff & color;
	}

	private static String toHexByte(int i) {
		if (i<0 || i>=256) {
			throw new IllegalArgumentException("bad byte: "+i);
		} else {
			StringBuilder sb = new StringBuilder(2);
			sb.append(toHexDigit(i>>4));
			sb.append(toHexDigit(i&15));
			return sb.toString();
		}
	}

	private static char toHexDigit(int i) {
		if (i<0 || i>=16) {
			throw new IllegalArgumentException("bad digit: "+i);
		} else if (i < 10) {
			return (char)('0' + i);
		} else {
			return (char)('a' + i - 10);
		}
	}

}
