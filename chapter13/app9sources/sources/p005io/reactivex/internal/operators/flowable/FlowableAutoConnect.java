package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.flowables.ConnectableFlowable;
import p005io.reactivex.functions.Consumer;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableAutoConnect */
/* loaded from: classes.dex */
public final class FlowableAutoConnect<T> extends Flowable<T> {
    final AtomicInteger clients = new AtomicInteger();
    final Consumer<? super Disposable> connection;
    final int numberOfSubscribers;
    final ConnectableFlowable<? extends T> source;

    public FlowableAutoConnect(ConnectableFlowable<? extends T> connectableFlowable, int i, Consumer<? super Disposable> consumer) {
        this.source = connectableFlowable;
        this.numberOfSubscribers = i;
        this.connection = consumer;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        this.source.subscribe((Subscriber<? super Object>) subscriber);
        if (this.clients.incrementAndGet() == this.numberOfSubscribers) {
            this.source.connect(this.connection);
        }
    }
}
