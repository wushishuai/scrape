package com.bumptech.glide.disklrucache;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/* loaded from: classes.dex */
class StrictLineReader implements Closeable {

    /* renamed from: CR */
    private static final byte f43CR = 13;

    /* renamed from: LF */
    private static final byte f44LF = 10;
    private byte[] buf;
    private final Charset charset;
    private int end;

    /* renamed from: in */
    private final InputStream f45in;
    private int pos;

    public StrictLineReader(InputStream in, Charset charset) {
        this(in, 8192, charset);
    }

    public StrictLineReader(InputStream in, int capacity, Charset charset) {
        if (in == null || charset == null) {
            throw new NullPointerException();
        } else if (capacity < 0) {
            throw new IllegalArgumentException("capacity <= 0");
        } else if (charset.equals(Util.US_ASCII)) {
            this.f45in = in;
            this.charset = charset;
            this.buf = new byte[capacity];
        } else {
            throw new IllegalArgumentException("Unsupported encoding");
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        synchronized (this.f45in) {
            if (this.buf != null) {
                this.buf = null;
                this.f45in.close();
            }
        }
    }

    public String readLine() throws IOException {
        int i;
        synchronized (this.f45in) {
            if (this.buf != null) {
                if (this.pos >= this.end) {
                    fillBuf();
                }
                int i2 = this.pos;
                while (i2 != this.end) {
                    if (this.buf[i2] == 10) {
                        String res = new String(this.buf, this.pos, ((i2 == this.pos || this.buf[i2 + -1] != 13) ? i2 : i2 - 1) - this.pos, this.charset.name());
                        this.pos = i2 + 1;
                        return res;
                    }
                    i2++;
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream((this.end - this.pos) + 80) { // from class: com.bumptech.glide.disklrucache.StrictLineReader.1
                    @Override // java.io.ByteArrayOutputStream, java.lang.Object
                    public String toString() {
                        try {
                            return new String(this.buf, 0, (this.count <= 0 || this.buf[this.count + -1] != 13) ? this.count : this.count - 1, StrictLineReader.this.charset.name());
                        } catch (UnsupportedEncodingException e) {
                            throw new AssertionError(e);
                        }
                    }
                };
                loop1: while (true) {
                    out.write(this.buf, this.pos, this.end - this.pos);
                    this.end = -1;
                    fillBuf();
                    i = this.pos;
                    while (i != this.end) {
                        if (this.buf[i] == 10) {
                            break loop1;
                        }
                        i++;
                    }
                }
                if (i != this.pos) {
                    out.write(this.buf, this.pos, i - this.pos);
                }
                this.pos = i + 1;
                return out.toString();
            }
            throw new IOException("LineReader is closed");
        }
    }

    public boolean hasUnterminatedLine() {
        return this.end == -1;
    }

    private void fillBuf() throws IOException {
        InputStream inputStream = this.f45in;
        byte[] bArr = this.buf;
        int result = inputStream.read(bArr, 0, bArr.length);
        if (result != -1) {
            this.pos = 0;
            this.end = result;
            return;
        }
        throw new EOFException();
    }
}
