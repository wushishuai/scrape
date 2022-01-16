package com.bumptech.glide.load.engine;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.Transformation;
import java.util.Map;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class EngineKeyFactory {
    /* JADX INFO: Access modifiers changed from: package-private */
    public EngineKey buildKey(Object model, Key signature, int width, int height, Map<Class<?>, Transformation<?>> transformations, Class<?> resourceClass, Class<?> transcodeClass, Options options) {
        return new EngineKey(model, signature, width, height, transformations, resourceClass, transcodeClass, options);
    }
}
