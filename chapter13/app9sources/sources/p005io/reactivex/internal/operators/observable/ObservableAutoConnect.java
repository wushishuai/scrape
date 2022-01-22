package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.observables.ConnectableObservable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableAutoConnect */
/* loaded from: classes.dex */
public final class ObservableAutoConnect<T> extends Observable<T> {
    final AtomicInteger clients = new AtomicInteger();
    final Consumer<? super Disposable> connection;
    final int numberOfObservers;
    final ConnectableObservable<? extends T> source;

    public ObservableAutoConnect(ConnectableObservable<? extends T> connectableObservable, int i, Consumer<? super Disposable> consumer) {
        this.source = connectableObservable;
        this.numberOfObservers = i;
        this.connection = consumer;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe((Observer<? super Object>) observer);
        if (this.clients.incrementAndGet() == this.numberOfObservers) {
            this.source.connect(this.connection);
        }
    }
}
