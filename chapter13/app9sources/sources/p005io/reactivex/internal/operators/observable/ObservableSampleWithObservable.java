package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.observers.SerializedObserver;

/* renamed from: io.reactivex.internal.operators.observable.ObservableSampleWithObservable */
/* loaded from: classes.dex */
public final class ObservableSampleWithObservable<T> extends AbstractObservableWithUpstream<T, T> {
    final boolean emitLast;
    final ObservableSource<?> other;

    public ObservableSampleWithObservable(ObservableSource<T> observableSource, ObservableSource<?> observableSource2, boolean z) {
        super(observableSource);
        this.other = observableSource2;
        this.emitLast = z;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        SerializedObserver serializedObserver = new SerializedObserver(observer);
        if (this.emitLast) {
            this.source.subscribe(new SampleMainEmitLast(serializedObserver, this.other));
        } else {
            this.source.subscribe(new SampleMainNoLast(serializedObserver, this.other));
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSampleWithObservable$SampleMainObserver */
    /* loaded from: classes.dex */
    static abstract class SampleMainObserver<T> extends AtomicReference<T> implements Observer<T>, Disposable {
        private static final long serialVersionUID = -3517602651313910099L;
        final Observer<? super T> downstream;
        final AtomicReference<Disposable> other = new AtomicReference<>();
        final ObservableSource<?> sampler;
        Disposable upstream;

        abstract void completeMain();

        abstract void completeOther();

        abstract void run();

        SampleMainObserver(Observer<? super T> observer, ObservableSource<?> observableSource) {
            this.downstream = observer;
            this.sampler = observableSource;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
                if (this.other.get() == null) {
                    this.sampler.subscribe(new SamplerObserver(this));
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            lazySet(t);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            DisposableHelper.dispose(this.other);
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            DisposableHelper.dispose(this.other);
            completeMain();
        }

        boolean setOther(Disposable disposable) {
            return DisposableHelper.setOnce(this.other, disposable);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this.other);
            this.upstream.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.other.get() == DisposableHelper.DISPOSED;
        }

        public void error(Throwable th) {
            this.upstream.dispose();
            this.downstream.onError(th);
        }

        public void complete() {
            this.upstream.dispose();
            completeOther();
        }

        void emit() {
            T andSet = getAndSet(null);
            if (andSet != null) {
                this.downstream.onNext(andSet);
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSampleWithObservable$SamplerObserver */
    /* loaded from: classes.dex */
    static final class SamplerObserver<T> implements Observer<Object> {
        final SampleMainObserver<T> parent;

        SamplerObserver(SampleMainObserver<T> sampleMainObserver) {
            this.parent = sampleMainObserver;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            this.parent.setOther(disposable);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(Object obj) {
            this.parent.run();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.parent.error(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.parent.complete();
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSampleWithObservable$SampleMainNoLast */
    /* loaded from: classes.dex */
    static final class SampleMainNoLast<T> extends SampleMainObserver<T> {
        private static final long serialVersionUID = -3029755663834015785L;

        SampleMainNoLast(Observer<? super T> observer, ObservableSource<?> observableSource) {
            super(observer, observableSource);
        }

        @Override // p005io.reactivex.internal.operators.observable.ObservableSampleWithObservable.SampleMainObserver
        void completeMain() {
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.internal.operators.observable.ObservableSampleWithObservable.SampleMainObserver
        void completeOther() {
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.internal.operators.observable.ObservableSampleWithObservable.SampleMainObserver
        void run() {
            emit();
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSampleWithObservable$SampleMainEmitLast */
    /* loaded from: classes.dex */
    static final class SampleMainEmitLast<T> extends SampleMainObserver<T> {
        private static final long serialVersionUID = -3029755663834015785L;
        volatile boolean done;
        final AtomicInteger wip = new AtomicInteger();

        SampleMainEmitLast(Observer<? super T> observer, ObservableSource<?> observableSource) {
            super(observer, observableSource);
        }

        @Override // p005io.reactivex.internal.operators.observable.ObservableSampleWithObservable.SampleMainObserver
        void completeMain() {
            this.done = true;
            if (this.wip.getAndIncrement() == 0) {
                emit();
                this.downstream.onComplete();
            }
        }

        @Override // p005io.reactivex.internal.operators.observable.ObservableSampleWithObservable.SampleMainObserver
        void completeOther() {
            this.done = true;
            if (this.wip.getAndIncrement() == 0) {
                emit();
                this.downstream.onComplete();
            }
        }

        @Override // p005io.reactivex.internal.operators.observable.ObservableSampleWithObservable.SampleMainObserver
        void run() {
            if (this.wip.getAndIncrement() == 0) {
                do {
                    boolean z = this.done;
                    emit();
                    if (z) {
                        this.downstream.onComplete();
                        return;
                    }
                } while (this.wip.decrementAndGet() != 0);
            }
        }
    }
}
