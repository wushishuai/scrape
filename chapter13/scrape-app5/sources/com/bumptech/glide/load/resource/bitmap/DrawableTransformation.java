package com.bumptech.glide.load.resource.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import java.security.MessageDigest;
/* loaded from: classes.dex */
public class DrawableTransformation implements Transformation<Drawable> {
    private final boolean isRequired;
    private final Transformation<Bitmap> wrapped;

    public DrawableTransformation(Transformation<Bitmap> wrapped, boolean isRequired) {
        this.wrapped = wrapped;
        this.isRequired = isRequired;
    }

    public Transformation<BitmapDrawable> asBitmapDrawable() {
        return this;
    }

    @Override // com.bumptech.glide.load.Transformation
    @NonNull
    public Resource<Drawable> transform(@NonNull Context context, @NonNull Resource<Drawable> resource, int outWidth, int outHeight) {
        BitmapPool bitmapPool = Glide.get(context).getBitmapPool();
        Drawable drawable = resource.get();
        Resource<Bitmap> bitmapResourceToTransform = DrawableToBitmapConverter.convert(bitmapPool, drawable, outWidth, outHeight);
        if (bitmapResourceToTransform != null) {
            Resource<Bitmap> transformedBitmapResource = this.wrapped.transform(context, bitmapResourceToTransform, outWidth, outHeight);
            if (!transformedBitmapResource.equals(bitmapResourceToTransform)) {
                return newDrawableResource(context, transformedBitmapResource);
            }
            transformedBitmapResource.recycle();
            return resource;
        } else if (!this.isRequired) {
            return resource;
        } else {
            throw new IllegalArgumentException("Unable to convert " + drawable + " to a Bitmap");
        }
    }

    private Resource<Drawable> newDrawableResource(Context context, Resource<Bitmap> transformed) {
        return LazyBitmapDrawableResource.obtain(context.getResources(), transformed);
    }

    @Override // com.bumptech.glide.load.Key
    public boolean equals(Object o) {
        if (o instanceof DrawableTransformation) {
            return this.wrapped.equals(((DrawableTransformation) o).wrapped);
        }
        return false;
    }

    @Override // com.bumptech.glide.load.Key
    public int hashCode() {
        return this.wrapped.hashCode();
    }

    @Override // com.bumptech.glide.load.Key
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        this.wrapped.updateDiskCacheKey(messageDigest);
    }
}