package okio;

import android.support.p000v4.media.session.PlaybackStateCompat;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.annotation.Nullable;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class RealBufferedSource implements BufferedSource {
    public final Buffer buffer = new Buffer();
    boolean closed;
    public final Source source;

    /* JADX INFO: Access modifiers changed from: package-private */
    public RealBufferedSource(Source source) {
        if (source != null) {
            this.source = source;
            return;
        }
        throw new NullPointerException("source == null");
    }

    @Override // okio.BufferedSource, okio.BufferedSink
    public Buffer buffer() {
        return this.buffer;
    }

    @Override // okio.Source
    public long read(Buffer buffer, long j) throws IOException {
        if (buffer == null) {
            throw new IllegalArgumentException("sink == null");
        } else if (j < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + j);
        } else if (this.closed) {
            throw new IllegalStateException("closed");
        } else if (this.buffer.size == 0 && this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1) {
            return -1;
        } else {
            return this.buffer.read(buffer, Math.min(j, this.buffer.size));
        }
    }

    @Override // okio.BufferedSource
    public boolean exhausted() throws IOException {
        if (!this.closed) {
            return this.buffer.exhausted() && this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1;
        }
        throw new IllegalStateException("closed");
    }

    @Override // okio.BufferedSource
    public void require(long j) throws IOException {
        if (!request(j)) {
            throw new EOFException();
        }
    }

    @Override // okio.BufferedSource
    public boolean request(long j) throws IOException {
        if (j < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + j);
        } else if (!this.closed) {
            while (this.buffer.size < j) {
                if (this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1) {
                    return false;
                }
            }
            return true;
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // okio.BufferedSource
    public byte readByte() throws IOException {
        require(1);
        return this.buffer.readByte();
    }

    @Override // okio.BufferedSource
    public ByteString readByteString() throws IOException {
        this.buffer.writeAll(this.source);
        return this.buffer.readByteString();
    }

    @Override // okio.BufferedSource
    public ByteString readByteString(long j) throws IOException {
        require(j);
        return this.buffer.readByteString(j);
    }

    @Override // okio.BufferedSource
    public int select(Options options) throws IOException {
        if (!this.closed) {
            do {
                int selectPrefix = this.buffer.selectPrefix(options);
                if (selectPrefix == -1) {
                    return -1;
                }
                long size = (long) options.byteStrings[selectPrefix].size();
                if (size <= this.buffer.size) {
                    this.buffer.skip(size);
                    return selectPrefix;
                }
            } while (this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) != -1);
            return -1;
        }
        throw new IllegalStateException("closed");
    }

    @Override // okio.BufferedSource
    public byte[] readByteArray() throws IOException {
        this.buffer.writeAll(this.source);
        return this.buffer.readByteArray();
    }

    @Override // okio.BufferedSource
    public byte[] readByteArray(long j) throws IOException {
        require(j);
        return this.buffer.readByteArray(j);
    }

    @Override // okio.BufferedSource
    public int read(byte[] bArr) throws IOException {
        return read(bArr, 0, bArr.length);
    }

    @Override // okio.BufferedSource
    public void readFully(byte[] bArr) throws IOException {
        try {
            require((long) bArr.length);
            this.buffer.readFully(bArr);
        } catch (EOFException e) {
            int i = 0;
            while (this.buffer.size > 0) {
                Buffer buffer = this.buffer;
                int read = buffer.read(bArr, i, (int) buffer.size);
                if (read != -1) {
                    i += read;
                } else {
                    throw new AssertionError();
                }
            }
            throw e;
        }
    }

    @Override // okio.BufferedSource
    public int read(byte[] bArr, int i, int i2) throws IOException {
        long j = (long) i2;
        Util.checkOffsetAndCount((long) bArr.length, (long) i, j);
        if (this.buffer.size == 0 && this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1) {
            return -1;
        }
        return this.buffer.read(bArr, i, (int) Math.min(j, this.buffer.size));
    }

    @Override // java.nio.channels.ReadableByteChannel
    public int read(ByteBuffer byteBuffer) throws IOException {
        if (this.buffer.size == 0 && this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1) {
            return -1;
        }
        return this.buffer.read(byteBuffer);
    }

    @Override // okio.BufferedSource
    public void readFully(Buffer buffer, long j) throws IOException {
        try {
            require(j);
            this.buffer.readFully(buffer, j);
        } catch (EOFException e) {
            buffer.writeAll(this.buffer);
            throw e;
        }
    }

    @Override // okio.BufferedSource
    public long readAll(Sink sink) throws IOException {
        if (sink != null) {
            long j = 0;
            while (this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) != -1) {
                long completeSegmentByteCount = this.buffer.completeSegmentByteCount();
                if (completeSegmentByteCount > 0) {
                    j += completeSegmentByteCount;
                    sink.write(this.buffer, completeSegmentByteCount);
                }
            }
            if (this.buffer.size() <= 0) {
                return j;
            }
            long size = j + this.buffer.size();
            Buffer buffer = this.buffer;
            sink.write(buffer, buffer.size());
            return size;
        }
        throw new IllegalArgumentException("sink == null");
    }

    @Override // okio.BufferedSource
    public String readUtf8() throws IOException {
        this.buffer.writeAll(this.source);
        return this.buffer.readUtf8();
    }

    @Override // okio.BufferedSource
    public String readUtf8(long j) throws IOException {
        require(j);
        return this.buffer.readUtf8(j);
    }

    @Override // okio.BufferedSource
    public String readString(Charset charset) throws IOException {
        if (charset != null) {
            this.buffer.writeAll(this.source);
            return this.buffer.readString(charset);
        }
        throw new IllegalArgumentException("charset == null");
    }

    @Override // okio.BufferedSource
    public String readString(long j, Charset charset) throws IOException {
        require(j);
        if (charset != null) {
            return this.buffer.readString(j, charset);
        }
        throw new IllegalArgumentException("charset == null");
    }

    @Override // okio.BufferedSource
    @Nullable
    public String readUtf8Line() throws IOException {
        long indexOf = indexOf((byte) 10);
        if (indexOf != -1) {
            return this.buffer.readUtf8Line(indexOf);
        }
        if (this.buffer.size != 0) {
            return readUtf8(this.buffer.size);
        }
        return null;
    }

    @Override // okio.BufferedSource
    public String readUtf8LineStrict() throws IOException {
        return readUtf8LineStrict(Long.MAX_VALUE);
    }

    @Override // okio.BufferedSource
    public String readUtf8LineStrict(long j) throws IOException {
        if (j >= 0) {
            long j2 = j == Long.MAX_VALUE ? Long.MAX_VALUE : j + 1;
            long indexOf = indexOf((byte) 10, 0, j2);
            if (indexOf != -1) {
                return this.buffer.readUtf8Line(indexOf);
            }
            if (j2 < Long.MAX_VALUE && request(j2) && this.buffer.getByte(j2 - 1) == 13 && request(1 + j2) && this.buffer.getByte(j2) == 10) {
                return this.buffer.readUtf8Line(j2);
            }
            Buffer buffer = new Buffer();
            Buffer buffer2 = this.buffer;
            buffer2.copyTo(buffer, 0, Math.min(32L, buffer2.size()));
            throw new EOFException("\\n not found: limit=" + Math.min(this.buffer.size(), j) + " content=" + buffer.readByteString().hex() + (char) 8230);
        }
        throw new IllegalArgumentException("limit < 0: " + j);
    }

    @Override // okio.BufferedSource
    public int readUtf8CodePoint() throws IOException {
        require(1);
        byte b = this.buffer.getByte(0);
        if ((b & 224) == 192) {
            require(2);
        } else if ((b & 240) == 224) {
            require(3);
        } else if ((b & 248) == 240) {
            require(4);
        }
        return this.buffer.readUtf8CodePoint();
    }

    @Override // okio.BufferedSource
    public short readShort() throws IOException {
        require(2);
        return this.buffer.readShort();
    }

    @Override // okio.BufferedSource
    public short readShortLe() throws IOException {
        require(2);
        return this.buffer.readShortLe();
    }

    @Override // okio.BufferedSource
    public int readInt() throws IOException {
        require(4);
        return this.buffer.readInt();
    }

    @Override // okio.BufferedSource
    public int readIntLe() throws IOException {
        require(4);
        return this.buffer.readIntLe();
    }

    @Override // okio.BufferedSource
    public long readLong() throws IOException {
        require(8);
        return this.buffer.readLong();
    }

    @Override // okio.BufferedSource
    public long readLongLe() throws IOException {
        require(8);
        return this.buffer.readLongLe();
    }

    @Override // okio.BufferedSource
    public long readDecimalLong() throws IOException {
        byte b;
        require(1);
        int i = 0;
        while (true) {
            int i2 = i + 1;
            if (!request((long) i2)) {
                break;
            }
            b = this.buffer.getByte((long) i);
            if ((b < 48 || b > 57) && !(i == 0 && b == 45)) {
                break;
            }
            i = i2;
        }
        if (i == 0) {
            throw new NumberFormatException(String.format("Expected leading [0-9] or '-' character but was %#x", Byte.valueOf(b)));
        }
        return this.buffer.readDecimalLong();
    }

    /* JADX WARN: Code restructure failed: missing block: B:18:0x0032, code lost:
        if (r1 == 0) goto L_0x0035;
     */
    /* JADX WARN: Code restructure failed: missing block: B:20:0x0049, code lost:
        throw new java.lang.NumberFormatException(java.lang.String.format("Expected leading [0-9a-fA-F] character but was %#x", java.lang.Byte.valueOf(r3)));
     */
    @Override // okio.BufferedSource
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public long readHexadecimalUnsignedLong() throws java.io.IOException {
        /*
            r6 = this;
            r0 = 1
            r6.require(r0)
            r0 = 0
            r1 = 0
        L_0x0007:
            int r2 = r1 + 1
            long r3 = (long) r2
            boolean r3 = r6.request(r3)
            if (r3 == 0) goto L_0x004a
            okio.Buffer r3 = r6.buffer
            long r4 = (long) r1
            byte r3 = r3.getByte(r4)
            r4 = 48
            if (r3 < r4) goto L_0x001f
            r4 = 57
            if (r3 <= r4) goto L_0x0030
        L_0x001f:
            r4 = 97
            if (r3 < r4) goto L_0x0027
            r4 = 102(0x66, float:1.43E-43)
            if (r3 <= r4) goto L_0x0030
        L_0x0027:
            r4 = 65
            if (r3 < r4) goto L_0x0032
            r4 = 70
            if (r3 <= r4) goto L_0x0030
            goto L_0x0032
        L_0x0030:
            r1 = r2
            goto L_0x0007
        L_0x0032:
            if (r1 == 0) goto L_0x0035
            goto L_0x004a
        L_0x0035:
            java.lang.NumberFormatException r1 = new java.lang.NumberFormatException
            r2 = 1
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.Byte r3 = java.lang.Byte.valueOf(r3)
            r2[r0] = r3
            java.lang.String r0 = "Expected leading [0-9a-fA-F] character but was %#x"
            java.lang.String r0 = java.lang.String.format(r0, r2)
            r1.<init>(r0)
            throw r1
        L_0x004a:
            okio.Buffer r0 = r6.buffer
            long r0 = r0.readHexadecimalUnsignedLong()
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.RealBufferedSource.readHexadecimalUnsignedLong():long");
    }

    @Override // okio.BufferedSource
    public void skip(long j) throws IOException {
        if (!this.closed) {
            while (j > 0) {
                if (this.buffer.size == 0 && this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1) {
                    throw new EOFException();
                }
                long min = Math.min(j, this.buffer.size());
                this.buffer.skip(min);
                j -= min;
            }
            return;
        }
        throw new IllegalStateException("closed");
    }

    @Override // okio.BufferedSource
    public long indexOf(byte b) throws IOException {
        return indexOf(b, 0, Long.MAX_VALUE);
    }

    @Override // okio.BufferedSource
    public long indexOf(byte b, long j) throws IOException {
        return indexOf(b, j, Long.MAX_VALUE);
    }

    @Override // okio.BufferedSource
    public long indexOf(byte b, long j, long j2) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        } else if (j < 0 || j2 < j) {
            throw new IllegalArgumentException(String.format("fromIndex=%s toIndex=%s", Long.valueOf(j), Long.valueOf(j2)));
        } else {
            while (j < j2) {
                long indexOf = this.buffer.indexOf(b, j, j2);
                if (indexOf != -1) {
                    return indexOf;
                }
                long j3 = this.buffer.size;
                if (j3 >= j2 || this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1) {
                    return -1;
                }
                j = Math.max(j, j3);
            }
            return -1;
        }
    }

    @Override // okio.BufferedSource
    public long indexOf(ByteString byteString) throws IOException {
        return indexOf(byteString, 0);
    }

    @Override // okio.BufferedSource
    public long indexOf(ByteString byteString, long j) throws IOException {
        if (!this.closed) {
            while (true) {
                long indexOf = this.buffer.indexOf(byteString, j);
                if (indexOf != -1) {
                    return indexOf;
                }
                long j2 = this.buffer.size;
                if (this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1) {
                    return -1;
                }
                j = Math.max(j, (j2 - ((long) byteString.size())) + 1);
            }
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // okio.BufferedSource
    public long indexOfElement(ByteString byteString) throws IOException {
        return indexOfElement(byteString, 0);
    }

    @Override // okio.BufferedSource
    public long indexOfElement(ByteString byteString, long j) throws IOException {
        if (!this.closed) {
            while (true) {
                long indexOfElement = this.buffer.indexOfElement(byteString, j);
                if (indexOfElement != -1) {
                    return indexOfElement;
                }
                long j2 = this.buffer.size;
                if (this.source.read(this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1) {
                    return -1;
                }
                j = Math.max(j, j2);
            }
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // okio.BufferedSource
    public boolean rangeEquals(long j, ByteString byteString) throws IOException {
        return rangeEquals(j, byteString, 0, byteString.size());
    }

    @Override // okio.BufferedSource
    public boolean rangeEquals(long j, ByteString byteString, int i, int i2) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        } else if (j < 0 || i < 0 || i2 < 0 || byteString.size() - i < i2) {
            return false;
        } else {
            for (int i3 = 0; i3 < i2; i3++) {
                long j2 = ((long) i3) + j;
                if (!(request(1 + j2) && this.buffer.getByte(j2) == byteString.getByte(i + i3))) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override // okio.BufferedSource
    public InputStream inputStream() {
        return new InputStream() { // from class: okio.RealBufferedSource.1
            @Override // java.io.InputStream
            public int read() throws IOException {
                if (RealBufferedSource.this.closed) {
                    throw new IOException("closed");
                } else if (RealBufferedSource.this.buffer.size == 0 && RealBufferedSource.this.source.read(RealBufferedSource.this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1) {
                    return -1;
                } else {
                    return RealBufferedSource.this.buffer.readByte() & 255;
                }
            }

            @Override // java.io.InputStream
            public int read(byte[] bArr, int i, int i2) throws IOException {
                if (!RealBufferedSource.this.closed) {
                    Util.checkOffsetAndCount((long) bArr.length, (long) i, (long) i2);
                    if (RealBufferedSource.this.buffer.size == 0 && RealBufferedSource.this.source.read(RealBufferedSource.this.buffer, PlaybackStateCompat.ACTION_PLAY_FROM_URI) == -1) {
                        return -1;
                    }
                    return RealBufferedSource.this.buffer.read(bArr, i, i2);
                }
                throw new IOException("closed");
            }

            @Override // java.io.InputStream
            public int available() throws IOException {
                if (!RealBufferedSource.this.closed) {
                    return (int) Math.min(RealBufferedSource.this.buffer.size, 2147483647L);
                }
                throw new IOException("closed");
            }

            @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
            public void close() throws IOException {
                RealBufferedSource.this.close();
            }

            @Override // java.lang.Object
            public String toString() {
                return RealBufferedSource.this + ".inputStream()";
            }
        };
    }

    @Override // java.nio.channels.Channel
    public boolean isOpen() {
        return !this.closed;
    }

    @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.source.close();
            this.buffer.clear();
        }
    }

    @Override // okio.Source
    public Timeout timeout() {
        return this.source.timeout();
    }

    @Override // java.lang.Object
    public String toString() {
        return "buffer(" + this.source + ")";
    }
}
