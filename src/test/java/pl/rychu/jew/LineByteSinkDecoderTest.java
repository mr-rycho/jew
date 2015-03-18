package pl.rychu.jew;

import static org.fest.assertions.Assertions.*;
import static junitparams.JUnitParamsRunner.$;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;



public class LineByteSinkDecoderTest {

	@Test
	public void wrapForShouldProduceCorrectStringPoses() {
		final List<Object[]> paramPacks = paramPacks();

		for (final Object[] params: paramPacks) {
			@SuppressWarnings("unchecked")
			final List<Object> events = (List<Object>)params[0];
			@SuppressWarnings("unchecked")
			final List<StringPosPair> expected = (List<StringPosPair>)params[1];

			shouldProduceCorrectStringPoses(events, expected);
		}
	}

	public void shouldProduceCorrectStringPoses(final List<Object> events
	 , final List<StringPosPair> expected) {
		// given
		final List<StringPosPair> act = new ArrayList<>();
		final LinePosSink sink = new LinePosSinkArray(act);
		final LineByteSink lineByteSink = new LineByteSinkDecoder(sink, "UTF-8");

		// when
		for (final Object event: events) {
			if (event instanceof ByteBuffer) {
				lineByteSink.put((ByteBuffer)event);
			} else if (event instanceof Number) {
				lineByteSink.lineBreak(((Number)event).longValue());
			} else {
				throw new IllegalStateException("unexpected: "+event);
			}
		}

		// then
		assertThat(act).isEqualTo(expected);
	}

	private List<Object[]> rawParametersForShouldProduceCorrectStringPoses() {
		final List<Object[]> result = new ArrayList<>();

		// correct utf8
		result.add($());
		result.add($(buf(0x41)));
		result.add($(buf(0x41), 0, buf(0x42), "A", 0, 1));
		result.add($(buf(0x41), 0, buf(0x42), 2, "A", 0, 1, "B", 2, 1));
		result.add($(buf(0x41), 0, buf(0x42), 3, "A", 0, 1, "B", 3, 1));
		result.add($(buf(0x41, 0xc4, 0x85), 0, buf(0x42), 4, "Aą", 0, 3, "B", 4, 1));
		result.add($(buf(0x41), buf(0xc4, 0x85), 0, buf(0x42), 4, "Aą", 0, 3, "B", 4, 1));
		result.add($(buf(0x41, 0xc4), buf(0x85), 0, buf(0x42), 4, "Aą", 0, 3, "B", 4, 1));

		for (int charCode=0x00; charCode<0x40; charCode++) {
			result.add($(buf(charCode), 0, buf(0x42), ""+((char)charCode), 0, 1));
			// yip, the LineByteSinkDecoder does not know that 0x0a is some
			// special character and if it's in the middle of the string it simply
			// decodes it.
		}

		// malformed
		for (int charCode=0x80; charCode<0x100; charCode++) {
			result.add($(buf(charCode), 0, buf(0x41), 2, "?", 0, 1, "A", 2, 1));
			result.add($(buf(0x41), buf(charCode), 0, buf(0x41), 2, "A?", 0, 2, "A", 2, 1));
			result.add($(buf(charCode), buf(0x41), 0, buf(0x41), 2, "?A", 0, 2, "A", 2, 1));
			result.add($(buf(0x61, charCode, 0x41), 0, buf(0x41), 2, "a?A", 0, 3, "A", 2, 1));
		}

		return result;
	}

	// ------------------

	protected List<Object[]> paramPacks() {
		final List<Object[]> rawParamPacks
		 = rawParametersForShouldProduceCorrectStringPoses();
		final List<Object[]> result = new ArrayList<>();

		for (final Object[] rawParams: rawParamPacks) {
			final Object[] params = interpretParams(rawParams);
			@SuppressWarnings("unchecked")
			final List<Object> events = (List<Object>)params[0];
			@SuppressWarnings("unchecked")
			final List<StringPosPair> resultLines = (List<StringPosPair>)params[1];

			result.add($(events, resultLines));
			/*
			final int blen = bytes.length;
			for (int bp=0; bp<=blen; bp++) {
				final ByteBuffer buf1 = ByteBuffer.allocate(bp);
				buf1.put(bytes, 0, bp);
				buf1.flip();
				final ByteBuffer buf2 = ByteBuffer.allocate(blen-bp);
				buf2.put(bytes, bp, blen-bp);
				buf2.flip();
				result.add($(Arrays.asList(buf1, buf2), resultLines));
			}
			*/
		}

		return result;
	}

	private static Object[] interpretParams(final Object[] objs) {
		final int split = firstString(objs);
		final Object[] toBeEvents = Arrays.copyOfRange(objs, 0, split);
		final Object[] toBeLines = Arrays.copyOfRange(objs, split, objs.length);

		final List<Object> resultEvents = toEvents(toBeEvents);
		final List<StringPosPair> resultLines = toLines(toBeLines);

		return $(resultEvents, resultLines);
	}

	private static int firstString(final Object[] objs) {
		final int len = objs.length;
		for (int i=0; i<len; i++) {
			final Object obj = objs[i];
			if (obj instanceof String) {
				return i;
			}
		}
		return len;
	}

	private static List<Object> toEvents(final Object[] objs) {
		return Arrays.asList(objs);
	}

	private static List<StringPosPair> toLines(final Object[] objs) {
		final int lenTwo = objs.length;
		if ((lenTwo % 3) != 0) {
			throw new IllegalArgumentException("number of args must be 3 div");
		}
		final int len = lenTwo / 3;
		final List<StringPosPair> result = new ArrayList<>(len);
		for (int i=0; i<len; i++) {
			final String line = (String)objs[i*3];
			final Number pos = (Number)objs[i*3+1];
			final Number lilen = (Number)objs[i*3+2];
			result.add(new StringPosPair(line, pos.longValue(), lilen.intValue()));
		}

		return result;
	}

	// ----------------

	private static ByteBuffer buf(int... ints) {
		return ByteBuffer.wrap(bytes(ints));
	}

	private static byte[] bytes(int... ints) {
		final int len = ints.length;
		final byte[] bytes = new byte[len];
		for (int i=0; i<len; i++) {
			final int in = ints[i];
			if (in<0 || i>255) {
				throw new IllegalArgumentException(""+in+" is not byte");
			}
			bytes[i] = (byte)in;
		}
		return bytes;
	}

	// ================

	public static class StringPosPair {
		private final String string;
		private final long pos;
		private final int length;

		public StringPosPair(String string, long pos, int length) {
			super();
			this.string = string;
			this.pos = pos;
			this.length = length;
		}

		protected String getString() {
			return string;
		}

		protected long getPos() {
			return pos;
		}

		protected int getLength() {
			return length;
		}

		@Override
		public String toString() {
			return "StringPosPair [string="+string+", pos="+pos
			 +", length="+length+"]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (pos ^ (pos >>> 32));
			result = prime * result + ((string == null) ? 0 : string.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj==null || getClass()!=obj.getClass()) return false;
			StringPosPair other = (StringPosPair) obj;
			if (pos != other.pos) return false;
			if (string == null) {
				if (other.string != null) return false;
			} else if (!string.equals(other.string))
				return false;
			return true;
		};

	}

	// ------------

	public static class LinePosSinkArray implements LinePosSink {

		private final List<StringPosPair> list;

		public LinePosSinkArray(final List<StringPosPair> list) {
			this.list = list;
		}

		@Override
		public void put(String line, long filePos, int length) {
			final StringPosPair spp = new StringPosPair(line, filePos, length);
			list.add(spp);
		}

	}

}
