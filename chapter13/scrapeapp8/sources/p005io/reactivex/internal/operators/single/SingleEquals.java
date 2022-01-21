package p005io.reactivex.internal.operators.single;

import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.single.SingleEquals */
/* loaded from: classes.dex */
public final class SingleEquals<T> extends Single<Boolean> {
    final SingleSource<? extends T> first;
    final SingleSource<? extends T> second;

    public SingleEquals(SingleSource<? extends T> first, SingleSource<? extends T> second) {
        this.first = first;
        this.second = second;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super Boolean> observer) {
        AtomicInteger count = new AtomicInteger();
        Object[] values = {null, null};
        CompositeDisposable set = new CompositeDisposable();
        observer.onSubscribe(set);
        this.first.subscribe(new InnerObserver(0, set, values, observer, count));
        this.second.subscribe(new InnerObserver(1, set, values, observer, count));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleEquals$InnerObserver */
    /* loaded from: classes.dex */
    static class InnerObserver<T> implements SingleObserver<T> {
        final AtomicInteger count;
        final SingleObserver<? super Boolean> downstream;
        final int index;
        final CompositeDisposable set;
        final Object[] values;

        InnerObserver(int index, CompositeDisposable set, Object[] values, SingleObserver<? super Boolean> observer, AtomicInteger count) {
            this.index = index;
            this.set = set;
            this.values = values;
            this.downstream = observer;
            this.count = count;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            this.set.add(d);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            this.values[this.index] = value;
            if (this.count.incrementAndGet() == 2) {
                SingleObserver<? super Boolean> singleObserver = this.downstream;
                Object[] objArr = this.values;
                singleObserver.onSuccess(Boolean.valueOf(ObjectHelper.equals(objArr[0], objArr[1])));
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            int state;
            do {
                state = this.count.get();
                if (state >= 2) {
                    RxJavaPlugins.onError(e);
                    return;
                }
            } while (!this.count.compareAndSet(state, 2));
            this.set.dispose();
            this.downstream.onError(e);
        }
    }
}
