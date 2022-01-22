package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.HalfSerializer;
import p005io.reactivex.subjects.PublishSubject;
import p005io.reactivex.subjects.Subject;

/* renamed from: io.reactivex.internal.operators.observable.ObservableRetryWhen */
/* loaded from: classes.dex */
public final class ObservableRetryWhen<T> extends AbstractObservableWithUpstream<T, T> {
    final Function<? super Observable<Throwable>, ? extends ObservableSource<?>> handler;

    public ObservableRetryWhen(ObservableSource<T> observableSource, Function<? super Observable<Throwable>, ? extends ObservableSource<?>> function) {
        super(observableSource);
        this.handler = function;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        Subject<T> serialized = PublishSubject.create().toSerialized();
        try {
            ObservableSource observableSource = (ObservableSource) ObjectHelper.requireNonNull(this.handler.apply(serialized), "The handler returned a null ObservableSource");
            RepeatWhenObserver repeatWhenObserver = new RepeatWhenObserver(observer, serialized, this.source);
            observer.onSubscribe(repeatWhenObserver);
            observableSource.subscribe(repeatWhenObserver.inner);
            repeatWhenObserver.subscribeNext();
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptyDisposable.error(th, observer);
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableRetryWhen$RepeatWhenObserver */
    /* loaded from: classes.dex */
    static final class RepeatWhenObserver<T> extends AtomicInteger implements Observer<T>, Disposable {
        private static final long serialVersionUID = 802743776666017014L;
        volatile boolean active;
        final Observer<? super T> downstream;
        final Subject<Throwable> signaller;
        final ObservableSource<T> source;
        final AtomicInteger wip = new AtomicInteger();
        final AtomicThrowable error = new AtomicThrowable();
        final RepeatWhenObserver<T>.InnerRepeatObserver inner = new InnerRepeatObserver();
        final AtomicReference<Disposable> upstream = new AtomicReference<>();

        RepeatWhenObserver(Observer<? super T> observer, Subject<Throwable> subject, ObservableSource<T> observableSource) {
            this.downstream = observer;
            this.signaller = subject;
            this.source = observableSource;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.replace(this.upstream, disposable);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            HalfSerializer.onNext(this.downstream, t, this, this.error);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.active = false;
            this.signaller.onNext(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            DisposableHelper.dispose(this.inner);
            HalfSerializer.onComplete(this.downstream, this, this.error);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(this.upstream.get());
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this.upstream);
            DisposableHelper.dispose(this.inner);
        }

        void innerNext() {
            subscribeNext();
        }

        void innerError(Throwable th) {
            DisposableHelper.dispose(this.upstream);
            HalfSerializer.onError(this.downstream, th, this, this.error);
        }

        void innerComplete() {
            DisposableHelper.dispose(this.upstream);
            HalfSerializer.onComplete(this.downstream, this, this.error);
        }

        void subscribeNext() {
            if (this.wip.getAndIncrement() == 0) {
                while (!isDisposed()) {
                    if (!this.active) {
                        this.active = true;
                        this.source.subscribe(this);
                    }
                    if (this.wip.decrementAndGet() == 0) {
                        return;
                    }
                }
            }
        }

        /* renamed from: io.reactivex.internal.operators.observable.ObservableRetryWhen$RepeatWhenObserver$InnerRepeatObserver */
        /* loaded from: classes.dex */
        final class InnerRepeatObserver extends AtomicReference<Disposable> implements Observer<Object> {
            private static final long serialVersionUID = 3254781284376480842L;

            InnerRepeatObserver() {
            }

            @Override // p005io.reactivex.Observer
            public void onSubscribe(Disposable disposable) {
                DisposableHelper.setOnce(this, disposable);
            }

            @Override // p005io.reactivex.Observer
            public void onNext(Object obj) {
                RepeatWhenObserver.this.innerNext();
            }

            @Override // p005io.reactivex.Observer
            public void onError(Throwable th) {
                RepeatWhenObserver.this.innerError(th);
            }

            @Override // p005io.reactivex.Observer
            public void onComplete() {
                RepeatWhenObserver.this.innerComplete();
            }
        }
    }
}
