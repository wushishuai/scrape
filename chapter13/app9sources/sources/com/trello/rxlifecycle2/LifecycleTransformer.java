package com.trello.rxlifecycle2;

import com.trello.rxlifecycle2.internal.Preconditions;
import javax.annotation.ParametersAreNonnullByDefault;
import org.reactivestreams.Publisher;
import p005io.reactivex.BackpressureStrategy;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.CompletableTransformer;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableTransformer;
import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.MaybeTransformer;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.ObservableTransformer;
import p005io.reactivex.Single;
import p005io.reactivex.SingleSource;
import p005io.reactivex.SingleTransformer;

@ParametersAreNonnullByDefault
/* loaded from: classes.dex */
public final class LifecycleTransformer<T> implements ObservableTransformer<T, T>, FlowableTransformer<T, T>, SingleTransformer<T, T>, MaybeTransformer<T, T>, CompletableTransformer {
    final Observable<?> observable;

    public LifecycleTransformer(Observable<?> observable) {
        Preconditions.checkNotNull(observable, "observable == null");
        this.observable = observable;
    }

    @Override // p005io.reactivex.ObservableTransformer
    public ObservableSource<T> apply(Observable<T> observable) {
        return observable.takeUntil(this.observable);
    }

    @Override // p005io.reactivex.FlowableTransformer
    public Publisher<T> apply(Flowable<T> flowable) {
        return flowable.takeUntil(this.observable.toFlowable(BackpressureStrategy.LATEST));
    }

    @Override // p005io.reactivex.SingleTransformer
    public SingleSource<T> apply(Single<T> single) {
        return single.takeUntil(this.observable.firstOrError());
    }

    @Override // p005io.reactivex.MaybeTransformer
    public MaybeSource<T> apply(Maybe<T> maybe) {
        return maybe.takeUntil(this.observable.firstElement());
    }

    @Override // p005io.reactivex.CompletableTransformer
    public CompletableSource apply(Completable completable) {
        return Completable.ambArray(completable, this.observable.flatMapCompletable(Functions.CANCEL_COMPLETABLE));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.observable.equals(((LifecycleTransformer) obj).observable);
    }

    public int hashCode() {
        return this.observable.hashCode();
    }

    public String toString() {
        return "LifecycleTransformer{observable=" + this.observable + '}';
    }
}
