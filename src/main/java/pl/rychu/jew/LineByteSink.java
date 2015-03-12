package pl.rychu.jew;

import java.nio.ByteBuffer;



public interface LineByteSink {

	void put(ByteBuffer byteBuffer);

	void lineBreak(long offset);

}
