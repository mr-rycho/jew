package pl.rychu.jew.logaccess;

import static org.fest.assertions.Assertions.*;
import static junitparams.JUnitParamsRunner.$;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import pl.rychu.jew.logaccess.LineByteSink;
import pl.rychu.jew.logaccess.LineByteSinkArray;
import pl.rychu.jew.logaccess.LineDividerUtf8;
import pl.rychu.jew.logaccess.LineByteSinkArray.LineByteSinkEvent;



public class LineDividerUtf8Test {

	private List<Object[]> parameters() {
		final List<Object[]> result = new ArrayList<>();

		result.add($(li(buf()), evs()));

		result.add($(li(buf(0x0a)), evs(0)));

		result.add($(li(buf(0x41, 0x0a)), evs(bytes(0x41), 0)));
		result.add($(li(buf(0x41), buf(0x0a)), evs(bytes(0x41), 0)));
		result.add($(li(buf(), buf(0x41), buf(), buf(0x0a), buf())
		 , evs(bytes(0x41), 0)));

		result.add($(li(buf(0x41, 0xc5, 0x0a)), evs(bytes(0x41, 0xc5), 0)));
		result.add($(li(buf(0x41, 0xc5), buf(0x0a)), evs(bytes(0x41, 0xc5), 0)));
		result.add($(li(buf(0x41), buf(0xc5), buf(0x0a))
		 , evs(bytes(0x41), bytes(0xc5), 0)));
		result.add($(li(buf(0x41), buf(0xc5, 0x0a))
		 , evs(bytes(0x41), bytes(0xc5), 0)));

		result.add($(li(buf(0x41, 0xc5, 0x0a, 0x49, 0x02, 0x0a, 0x61))
		 , evs(bytes(0x41, 0xc5), 0, bytes(0x49, 0x02), 3, bytes(0x61))));
		result.add($(li(buf(0x41), buf(0xc5, 0x0a, 0x49), buf(0x02, 0x0a, 0x61))
		 , evs(bytes(0x41), bytes(0xc5), 0, bytes(0x49), bytes(0x02), 3, bytes(0x61))));
		result.add($(li(buf(0x41), buf(0xc5), buf(0x0a, 0x49), buf(0x02), buf(0x0a, 0x61))
		 , evs(bytes(0x41), bytes(0xc5), 0, bytes(0x49), bytes(0x02), 3, bytes(0x61))));
		result.add($(li(buf(0x41), buf(0xc5), buf(0x0a), buf(0x49, 0x02), buf(0x0a, 0x61))
		 , evs(bytes(0x41), bytes(0xc5), 0, bytes(0x49, 0x02), 3, bytes(0x61))));

		result.add($(li(buf(0x41), buf(0xc5), buf(0x0d), buf(0x49, 0x02), buf(0x0a, 0x61))
		 , evs(bytes(0x41), bytes(0xc5), 0, bytes(0x49, 0x02), 3, bytes(0x61))));
		result.add($(li(buf(0x41), buf(0xc5), buf(0x0d, 0x0a), buf(0x49, 0x02), buf(0x0a, 0x61))
		 , evs(bytes(0x41), bytes(0xc5), 0, bytes(0x49, 0x02), 4, bytes(0x61))));
		result.add($(li(buf(0x41), buf(0xc5), buf(0x0d, 0x0a, 0x0d), buf(0x49, 0x02), buf(0x0a, 0x61))
		 , evs(bytes(0x41), bytes(0xc5), 0, bytes(0x49, 0x02), 5, bytes(0x61))));
		result.add($(li(buf(0x41), buf(0xc5), buf(0x0d, 0x0a), buf(0x0d, 0x49, 0x02), buf(0x0a, 0x61))
		 , evs(bytes(0x41), bytes(0xc5), 0, bytes(0x49, 0x02), 5, bytes(0x61))));
		return result;
	}

	// ---

	public void shouldProduceExpectedOutput(final List<ByteBuffer> buffers
	 , final List<LineByteSinkEvent> expEvents) {
		// given
		final List<LineByteSinkEvent> actEvents = new ArrayList<>();
		final LineByteSink lineByteSink = new LineByteSinkArray(actEvents);
		final LineDividerUtf8 lineDivider = new LineDividerUtf8(lineByteSink);

		// when
		for (final ByteBuffer buffer: buffers) {
			lineDivider.put(buffer);
		}

		// then
		assertThat(actEvents).isEqualTo(expEvents);
	}

	// ------------

	@Test
	public void shouldProduceExpectedOutputWrapper() {
		final List<Object[]> paramPacks = parameters();

		for (final Object[] params: paramPacks) {
			@SuppressWarnings("unchecked")
			final List<ByteBuffer> buffers = (List<ByteBuffer>)params[0];
			@SuppressWarnings("unchecked")
			final List<LineByteSinkEvent> events = (List<LineByteSinkEvent>)params[1];
			shouldProduceExpectedOutput(buffers, events);
		}
	}

	private static List<ByteBuffer> li(final ByteBuffer... buffers) {
		return Arrays.asList(buffers);
	}

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

	private static List<LineByteSinkEvent> evs(final Object... objs) {
		final int len = objs.length;
		final List<LineByteSinkEvent> result
		 = new ArrayList<>(Math.min(2, len));
		for (final Object obj: objs) {
			if (obj instanceof byte[]) {
				final byte[] bytes = (byte[])obj;
				result.add(by(bytes));
			} else if (obj instanceof Number) {
				final long offset = ((Number)obj).longValue();
				result.add(nl(offset));
			} else {
				throw new IllegalArgumentException("bad obj: "+obj);
			}
		}
		return result;
	}

	private static LineByteSinkEvent by(final byte[] bytes) {
		return LineByteSinkEvent.getBytesEvent(bytes);
	}

	private static LineByteSinkEvent nl(final long offset) {
		return LineByteSinkEvent.getNewlineEvent(offset);
	}

}
