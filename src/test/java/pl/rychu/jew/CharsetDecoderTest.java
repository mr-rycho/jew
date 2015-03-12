package pl.rychu.jew;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import org.junit.Test;

public class CharsetDecoderTest {

	// StreamDecoder extends Reader

	@Test
	public void test() {
		final Charset utf8Charset = Charset.forName("UTF8");
		System.out.println("charset: "+utf8Charset);//TODO gtfo
		final CharsetDecoder decoder = utf8Charset.newDecoder();
		System.out.println("decoder: "+decoder);//TODO gtfo

		final ByteBuffer byteBuffer = ByteBuffer.allocate(100);
		System.out.println("byteBuffer: "+byteBuffer);//TODO gtfo
		final CharBuffer charBuffer = CharBuffer.allocate(100);
		System.out.println("charBuffer: "+charBuffer);//TODO gtfo

		try {
			CharBuffer result = decoder.decode(byteBuffer);
			System.out.println("result: "+result);
			System.out.println("byteBuffer: "+byteBuffer);
		} catch (CharacterCodingException e) {
			System.out.println("error: "+e.getMessage());
			System.out.println("error: "+e.getClass().getCanonicalName());
		}

		try {
			byteBuffer.clear();
			byteBuffer.put((byte)'a');
			byteBuffer.put((byte)'b');
			byteBuffer.put((byte)'c');
			byteBuffer.flip();

			CharBuffer result = decoder.decode(byteBuffer);
			System.out.println("result: "+result);
			System.out.println("byteBuffer: "+byteBuffer);
		} catch (CharacterCodingException e) {
			System.out.println("error: "+e.getMessage());
			System.out.println("error: "+e.getClass().getCanonicalName());
		}

		try {
			byteBuffer.clear();
			byteBuffer.put((byte)0x70);
			byteBuffer.put((byte)0xc4);
			byteBuffer.put((byte)0x85);
			byteBuffer.put((byte)0x6b);
			byteBuffer.flip();

			CharBuffer result = decoder.decode(byteBuffer);
			System.out.println("result: "+result);
			System.out.println("byteBuffer: "+byteBuffer);
		} catch (CharacterCodingException e) {
			System.out.println("error: "+e.getMessage());
			System.out.println("error: "+e.getClass().getCanonicalName());
		}

		try {
			byteBuffer.clear();
			byteBuffer.put((byte)0x6b);
			byteBuffer.put((byte)0x70);
			byteBuffer.put((byte)0xc4);
			byteBuffer.flip();

			CharBuffer result = decoder.decode(byteBuffer);
			System.out.println("result: "+result);
			System.out.println("byteBuffer: "+byteBuffer);
		} catch (CharacterCodingException e) {
			System.out.println("error: "+e.getMessage());
			System.out.println("error: "+e.getClass().getCanonicalName());
		}

		try {
			System.out.println("dd");
			byteBuffer.clear();
			byteBuffer.put((byte)0x6b);
			byteBuffer.put((byte)0x70);
			byteBuffer.put((byte)0xc4);
			byteBuffer.put((byte)0x70);
			// byteBuffer.put((byte)0xc4);
			byteBuffer.flip();
			System.out.println("byteBuffer: "+byteBuffer);
			charBuffer.clear();
			// charBuffer.flip();
			System.out.println("charBuffer[pos="+charBuffer.position()
			 +" lim="+charBuffer.limit()+"]");
			decoder.reset();

			CoderResult result = decoder.decode(byteBuffer, charBuffer, false);
			System.out.println("result: "+result);
			System.out.println("byteBuffer: "+byteBuffer);
			System.out.println("charBuffer: "+charBuffer);
			System.out.println("charBuffer[pos="+charBuffer.position()
					 +" lim="+charBuffer.limit()+"]");
			charBuffer.flip();
			System.out.println("charBuffer[pos="+charBuffer.position()
					 +" lim="+charBuffer.limit()+"]");
			System.out.println("charBuffer: "+charBuffer);
		} catch (Exception e) {
			System.out.println("error: "+e.getMessage());
			System.out.println("error: "+e.getClass().getCanonicalName());
			System.out.println("charBuffer: "+charBuffer);
			System.out.println("charBuffer[pos="+charBuffer.position()
			 +" lim="+charBuffer.limit()+"]");
			e.printStackTrace();
		}
	}

}
