package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeUnsafeCreate */
/* loaded from: classes.dex */
public final class MaybeUnsafeCreate<T> extends AbstractMaybeWithUpstream<T, T> {
    public MaybeUnsafeCreate(MaybeSource<T> maybeSource) {
        super(maybeSource);
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.source.subscribe(maybeObserver);
    }
}
