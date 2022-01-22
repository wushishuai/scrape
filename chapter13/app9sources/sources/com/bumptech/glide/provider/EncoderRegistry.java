package com.bumptech.glide.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.load.Encoder;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class EncoderRegistry {
    private final List<Entry<?>> encoders = new ArrayList();

    @Nullable
    public synchronized <T> Encoder<T> getEncoder(@NonNull Class<T> cls) {
        for (Entry<?> entry : this.encoders) {
            if (entry.handles(cls)) {
                return (Encoder<T>) entry.encoder;
            }
        }
        return null;
    }

    public synchronized <T> void append(@NonNull Class<T> cls, @NonNull Encoder<T> encoder) {
        this.encoders.add(new Entry<>(cls, encoder));
    }

    public synchronized <T> void prepend(@NonNull Class<T> cls, @NonNull Encoder<T> encoder) {
        this.encoders.add(0, new Entry<>(cls, encoder));
    }

    /* loaded from: classes.dex */
    private static final class Entry<T> {
        private final Class<T> dataClass;
        final Encoder<T> encoder;

        Entry(@NonNull Class<T> cls, @NonNull Encoder<T> encoder) {
            this.dataClass = cls;
            this.encoder = encoder;
        }

        boolean handles(@NonNull Class<?> cls) {
            return this.dataClass.isAssignableFrom(cls);
        }
    }
}
