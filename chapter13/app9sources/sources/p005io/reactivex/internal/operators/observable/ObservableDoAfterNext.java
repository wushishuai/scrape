package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.observers.BasicFuseableObserver;

/* renamed from: io.reactivex.internal.operators.observable.ObservableDoAfterNext */
/* loaded from: classes.dex */
public final class ObservableDoAfterNext<T> extends AbstractObservableWithUpstream<T, T> {
    final Consumer<? super T> onAfterNext;

    public ObservableDoAfterNext(ObservableSource<T> observableSource, Consumer<? super T> consumer) {
        super(observableSource);
        this.onAfterNext = consumer;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new DoAfterObserver(observer, this.onAfterNext));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableDoAfterNext$DoAfterObserver */
    /* loaded from: classes.dex */
    static final class DoAfterObserver<T> extends BasicFuseableObserver<T, T> {
        final Consumer<? super T> onAfterNext;

        DoAfterObserver(Observer<? super T> observer, Consumer<? super T> consumer) {
            super(observer);
            this.onAfterNext = consumer;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.downstream.onNext(t);
            if (this.sourceMode == 0) {
                try {
                    this.onAfterNext.accept(t);
                } catch (Throwable th) {
                    fail(th);
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            return transitiveBoundaryFusion(i);
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            T poll = this.f99qd.poll();
            if (poll != null) {
                this.onAfterNext.accept(poll);
            }
            return poll;
        }
    }
}
