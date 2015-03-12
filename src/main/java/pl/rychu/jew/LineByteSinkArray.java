package pl.rychu.jew;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class LineByteSinkArray implements LineByteSink {

	private final List<LineByteSinkEvent> list;

	// ------

	public LineByteSinkArray(final List<LineByteSinkEvent> list) {
		if (list == null) {
			throw new IllegalArgumentException("list cannot be null");
		}
		this.list = list;
	}

	// ------

	@Override
	public void put(final ByteBuffer byteBuffer) {
		list.add(LineByteSinkEvent.getBytesEvent(byteBuffer));
	}

	@Override
	public void lineBreak(long offset) {
		list.add(LineByteSinkEvent.getNewlineEvent(offset));
	}

	// ============

	public static class LineByteSinkEvent {
		private final byte[] bytes;
		private final long offset;

		private LineByteSinkEvent(final byte[] bytes, final long offset) {
			if (bytes == null) {
				this.bytes = null;
			} else {
				this.bytes = Arrays.copyOf(bytes, bytes.length);
			}
			this.offset = offset;
		}

		public static LineByteSinkEvent getBytesEvent(final ByteBuffer buffer) {
			if (buffer.hasArray()) {
				final int size = buffer.remaining();
				final byte[] bytes = new byte[size];
				System.arraycopy(buffer.array(), buffer.position()+buffer.arrayOffset()
				 , bytes, 0, size);
				return getBytesEvent(bytes);
			} else {
				throw new UnsupportedOperationException();
			}
		}

		public static LineByteSinkEvent getBytesEvent(final byte[] bytes) {
			if (bytes == null) {
				throw new IllegalStateException("bytes must not be null");
			}
			return new LineByteSinkEvent(bytes, 0L);
		}

		public static LineByteSinkEvent getNewlineEvent(final long offset) {
			return new LineByteSinkEvent(null, offset);
		}

		public boolean isBytesEvent() {
			return bytes != null;
		}

		public boolean isNewlineEvent() {
			return bytes == null;
		}

		public byte[] getBytes() {
			if (bytes == null) {
				throw new IllegalStateException("this is a newline event");
			}
			return Arrays.copyOf(bytes, bytes.length);
		}

		public long getOffset() {
			if (bytes != null) {
				throw new IllegalStateException("this is a bytes event");
			}
			return offset;
		}

		@Override
		public String toString() {
			if (bytes!=null) {
				final int len = bytes.length;
				final StringBuilder sb = new StringBuilder(len*5+10);
				sb.append("by[");
				for (int i=0; i<len; i++) {
					if (i != 0) sb.append(",");
					sb.append(String.format("0x%02x", 0xff & bytes[i]));
				}
				sb.append("]");
				return sb.toString();
			} else {
				return "nl["+offset+"]";
			}
		}

		@Override
		public int hashCode() {
			if (bytes!=null) {
				return bytes.hashCode();
			} else {
				return (int)(offset ^ (offset>>>32));
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj==null || getClass()!=obj.getClass()) return false;
			final LineByteSinkEvent other = (LineByteSinkEvent)obj;
			if (isBytesEvent() ^ other.isBytesEvent()) return false;
			if (isBytesEvent()) {
				final byte[] ob = other.getBytes();
				if (bytes.length != ob.length) return false;
				for (int i=0; i<bytes.length; i++) {
					if (bytes[i] != ob[i]) return false;
				}
				return true;
			} else {
				return getOffset() == other.getOffset();
			}
		}
	}

}
