package com.bumptech.glide.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.drawable.ResourceDrawableDecoder;

/* loaded from: classes.dex */
public class ResourceBitmapDecoder implements ResourceDecoder<Uri, Bitmap> {
    private final BitmapPool bitmapPool;
    private final ResourceDrawableDecoder drawableDecoder;

    public ResourceBitmapDecoder(ResourceDrawableDecoder drawableDecoder, BitmapPool bitmapPool) {
        this.drawableDecoder = drawableDecoder;
        this.bitmapPool = bitmapPool;
    }

    public boolean handles(@NonNull Uri source, @NonNull Options options) {
        return "android.resource".equals(source.getScheme());
    }

    @Nullable
    public Resource<Bitmap> decode(@NonNull Uri source, int width, int height, @NonNull Options options) {
        Resource<Drawable> drawableResource = this.drawableDecoder.decode(source, width, height, options);
        if (drawableResource == null) {
            return null;
        }
        return DrawableToBitmapConverter.convert(this.bitmapPool, drawableResource.get(), width, height);
    }
}
