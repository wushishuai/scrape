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

/* renamed from: io.reactivex.internal.operators.observable.ObservableRepeatWhen */
/* loaded from: classes.dex */
public final class ObservableRepeatWhen<T> extends AbstractObservableWithUpstream<T, T> {
    final Function<? super Observable<Object>, ? extends ObservableSource<?>> handler;

    public ObservableRepeatWhen(ObservableSource<T> source, Function<? super Observable<Object>, ? extends ObservableSource<?>> handler) {
        super(source);
        this.handler = handler;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        Subject<Object> signaller = PublishSubject.create().toSerialized();
        try {
            ObservableSource<?> other = (ObservableSource) ObjectHelper.requireNonNull(this.handler.apply(signaller), "The handler returned a null ObservableSource");
            RepeatWhenObserver<T> parent = new RepeatWhenObserver<>(observer, signaller, this.source);
            observer.onSubscribe(parent);
            other.subscribe(parent.inner);
            parent.subscribeNext();
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableRepeatWhen$RepeatWhenObserver */
    /* loaded from: classes.dex */
    static final class RepeatWhenObserver<T> extends AtomicInteger implements Observer<T>, Disposable {
        private static final long serialVersionUID = 802743776666017014L;
        volatile boolean active;
        final Observer<? super T> downstream;
        final Subject<Object> signaller;
        final ObservableSource<T> source;
        final AtomicInteger wip = new AtomicInteger();
        final AtomicThrowable error = new AtomicThrowable();
        final RepeatWhenObserver<T>.InnerRepeatObserver inner = new InnerRepeatObserver();
        final AtomicReference<Disposable> upstream = new AtomicReference<>();

        RepeatWhenObserver(Observer<? super T> actual, Subject<Object> signaller, ObservableSource<T> source) {
            this.downstream = actual;
            this.signaller = signaller;
            this.source = source;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            DisposableHelper.replace(this.upstream, d);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            HalfSerializer.onNext(this.downstream, t, this, this.error);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            DisposableHelper.dispose(this.inner);
            HalfSerializer.onError(this.downstream, e, this, this.error);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.active = false;
            this.signaller.onNext(0);
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

        void innerError(Throwable ex) {
            DisposableHelper.dispose(this.upstream);
            HalfSerializer.onError(this.downstream, ex, this, this.error);
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

        /* renamed from: io.reactivex.internal.operators.observable.ObservableRepeatWhen$RepeatWhenObserver$InnerRepeatObserver */
        /* loaded from: classes.dex */
        final class InnerRepeatObserver extends AtomicReference<Disposable> implements Observer<Object> {
            private static final long serialVersionUID = 3254781284376480842L;

            InnerRepeatObserver() {
            }

            @Override // p005io.reactivex.Observer
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override // p005io.reactivex.Observer
            public void onNext(Object t) {
                RepeatWhenObserver.this.innerNext();
            }

            @Override // p005io.reactivex.Observer
            public void onError(Throwable e) {
                RepeatWhenObserver.this.innerError(e);
            }

            @Override // p005io.reactivex.Observer
            public void onComplete() {
                RepeatWhenObserver.this.innerComplete();
            }
        }
    }
}
