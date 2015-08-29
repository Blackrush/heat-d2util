package org.heat.dofus.network.netty;

import io.netty.buffer.ByteBuf;
import org.behaviorismanaged.core.io.DataReader;
import org.heat.shared.io.LegacyReader;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public final class NettyDataReader extends LegacyReader {
    private final ByteBuf buf;

    public NettyDataReader(ByteBuf buf) {
        this.buf = Objects.requireNonNull(buf, "buf").order(ByteOrder.BIG_ENDIAN);
    }

    public static NettyDataReader of(ByteBuf buf) {
        return new NettyDataReader(buf);
    }

    @Override
    public long getPosition() {
        return buf.readerIndex();
    }

    @Override
    public void setPosition(long position) {
        buf.readerIndex((int) position);
    }

    @Override
    public boolean readable(int n) {
        return buf.isReadable(n);
    }

    @Override
    public byte[] readBytes(int n) {
        byte[] bytes = new byte[n];
        buf.readBytes(bytes);
        return bytes;
    }

    @Override
    public DataReader read(int n) {
        return new NettyDataReader(buf.readSlice(n));
    }

    @Override
    public ByteBuffer all() {
        return buf.nioBuffer();
    }

    @Override
    public byte[] allBytes() {
        ByteBuf dup = buf.duplicate().resetReaderIndex();
        byte[] res = new byte[dup.readableBytes()];
        dup.readBytes(res);
        return res;
    }

    @Override
    public byte readInt8() {
        return buf.readByte();
    }

    @Override
    public short readUInt8() {
        return buf.readUnsignedByte();
    }

    @Override
    public short readInt16() {
        return buf.readShort();
    }

    @Override
    public int readUInt16() {
        return buf.readUnsignedShort();
    }

    @Override
    public int readInt32() {
        return buf.readInt();
    }

    @Override
    public long readUInt32() {
        return buf.readUnsignedInt();
    }

    @Override
    public long readInt64() {
        return buf.readLong();
    }

    @Override
    public BigInteger readUInt64() {
        return BigInteger.valueOf(readUInt32()).shiftLeft(32).or(BigInteger.valueOf(readUInt32()));
    }
}