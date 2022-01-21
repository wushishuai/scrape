package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Publisher;
import p005io.reactivex.Flowable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.HasUpstreamPublisher;

/* JADX INFO: Access modifiers changed from: package-private */
/* renamed from: io.reactivex.internal.operators.flowable.AbstractFlowableWithUpstream */
/* loaded from: classes.dex */
public abstract class AbstractFlowableWithUpstream<T, R> extends Flowable<R> implements HasUpstreamPublisher<T> {
    protected final Flowable<T> source;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AbstractFlowableWithUpstream(Flowable<T> source) {
        this.source = (Flowable) ObjectHelper.requireNonNull(source, "source is null");
    }

    @Override // p005io.reactivex.internal.fuseable.HasUpstreamPublisher
    public final Publisher<T> source() {
        return this.source;
    }
}
