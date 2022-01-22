package com.bumptech.glide.load.resource.bytes;

import android.support.annotation.NonNull;
import com.bumptech.glide.load.data.DataRewinder;
import java.nio.ByteBuffer;

/* loaded from: classes.dex */
public class ByteBufferRewinder implements DataRewinder<ByteBuffer> {
    private final ByteBuffer buffer;

    @Override // com.bumptech.glide.load.data.DataRewinder
    public void cleanup() {
    }

    public ByteBufferRewinder(ByteBuffer byteBuffer) {
        this.buffer = byteBuffer;
    }

    @Override // com.bumptech.glide.load.data.DataRewinder
    @NonNull
    public ByteBuffer rewindAndGet() {
        this.buffer.position(0);
        return this.buffer;
    }

    /* loaded from: classes.dex */
    public static class Factory implements DataRewinder.Factory<ByteBuffer> {
        @NonNull
        public DataRewinder<ByteBuffer> build(ByteBuffer byteBuffer) {
            return new ByteBufferRewinder(byteBuffer);
        }

        @Override // com.bumptech.glide.load.data.DataRewinder.Factory
        @NonNull
        public Class<ByteBuffer> getDataClass() {
            return ByteBuffer.class;
        }
    }
}
