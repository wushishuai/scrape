package com.bumptech.glide.load.engine.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.cache.MemoryCache;
/* loaded from: classes.dex */
public class MemoryCacheAdapter implements MemoryCache {
    private MemoryCache.ResourceRemovedListener listener;

    @Override // com.bumptech.glide.load.engine.cache.MemoryCache
    public long getCurrentSize() {
        return 0;
    }

    @Override // com.bumptech.glide.load.engine.cache.MemoryCache
    public long getMaxSize() {
        return 0;
    }

    @Override // com.bumptech.glide.load.engine.cache.MemoryCache
    public void setSizeMultiplier(float multiplier) {
    }

    @Override // com.bumptech.glide.load.engine.cache.MemoryCache
    @Nullable
    public Resource<?> remove(@NonNull Key key) {
        return null;
    }

    @Override // com.bumptech.glide.load.engine.cache.MemoryCache
    @Nullable
    public Resource<?> put(@NonNull Key key, @Nullable Resource<?> resource) {
        if (resource == null) {
            return null;
        }
        this.listener.onResourceRemoved(resource);
        return null;
    }

    @Override // com.bumptech.glide.load.engine.cache.MemoryCache
    public void setResourceRemovedListener(@NonNull MemoryCache.ResourceRemovedListener listener) {
        this.listener = listener;
    }

    @Override // com.bumptech.glide.load.engine.cache.MemoryCache
    public void clearMemory() {
    }

    @Override // com.bumptech.glide.load.engine.cache.MemoryCache
    public void trimMemory(int level) {
    }
}