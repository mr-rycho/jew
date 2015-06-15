package pl.rychu.jew.logaccess;

import java.nio.ByteBuffer;



public interface LineByteSink {

	void put(ByteBuffer byteBuffer);

	void lineBreak(long offset);

	void reset();

}
