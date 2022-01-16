package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.internal.fuseable.HasUpstreamMaybeSource;

/* renamed from: io.reactivex.internal.operators.maybe.AbstractMaybeWithUpstream */
/* loaded from: classes.dex */
public abstract class AbstractMaybeWithUpstream<T, R> extends Maybe<R> implements HasUpstreamMaybeSource<T> {
    protected final MaybeSource<T> source;

    public AbstractMaybeWithUpstream(MaybeSource<T> source) {
        this.source = source;
    }

    @Override // p005io.reactivex.internal.fuseable.HasUpstreamMaybeSource
    public final MaybeSource<T> source() {
        return this.source;
    }
}
