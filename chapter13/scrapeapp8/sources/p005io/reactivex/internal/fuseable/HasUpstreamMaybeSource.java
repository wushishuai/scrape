package p005io.reactivex.internal.fuseable;

import p005io.reactivex.MaybeSource;

/* renamed from: io.reactivex.internal.fuseable.HasUpstreamMaybeSource */
/* loaded from: classes.dex */
public interface HasUpstreamMaybeSource<T> {
    MaybeSource<T> source();
}
