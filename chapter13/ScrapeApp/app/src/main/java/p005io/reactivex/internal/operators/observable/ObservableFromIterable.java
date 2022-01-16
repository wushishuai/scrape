package p005io.reactivex.internal.operators.observable;

import java.util.Iterator;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.observers.BasicQueueDisposable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableFromIterable */
/* loaded from: classes.dex */
public final class ObservableFromIterable<T> extends Observable<T> {
    final Iterable<? extends T> source;

    public ObservableFromIterable(Iterable<? extends T> source) {
        this.source = source;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        try {
            Iterator<? extends T> it = this.source.iterator();
            try {
                if (!it.hasNext()) {
                    EmptyDisposable.complete(observer);
                    return;
                }
                FromIterableDisposable<T> d = new FromIterableDisposable<>(observer, it);
                observer.onSubscribe(d);
                if (!d.fusionMode) {
                    d.run();
                }
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                EmptyDisposable.error(e, observer);
            }
        } catch (Throwable e2) {
            Exceptions.throwIfFatal(e2);
            EmptyDisposable.error(e2, observer);
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableFromIterable$FromIterableDisposable */
    /* loaded from: classes.dex */
    static final class FromIterableDisposable<T> extends BasicQueueDisposable<T> {
        boolean checkNext;
        volatile boolean disposed;
        boolean done;
        final Observer<? super T> downstream;
        boolean fusionMode;

        /* renamed from: it */
        final Iterator<? extends T> f154it;

        FromIterableDisposable(Observer<? super T> actual, Iterator<? extends T> it) {
            this.downstream = actual;
            this.f154it = it;
        }

        void run() {
            while (!isDisposed()) {
                try {
                    this.downstream.onNext(ObjectHelper.requireNonNull(this.f154it.next(), "The iterator returned a null value"));
                    if (!isDisposed()) {
                        try {
                            if (!this.f154it.hasNext()) {
                                if (!isDisposed()) {
                                    this.downstream.onComplete();
                                    return;
                                }
                                return;
                            }
                        } catch (Throwable e) {
                            Exceptions.throwIfFatal(e);
                            this.downstream.onError(e);
                            return;
                        }
                    } else {
                        return;
                    }
                } catch (Throwable e2) {
                    Exceptions.throwIfFatal(e2);
                    this.downstream.onError(e2);
                    return;
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            if ((mode & 1) == 0) {
                return 0;
            }
            this.fusionMode = true;
            return 1;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() {
            if (this.done) {
                return null;
            }
            if (!this.checkNext) {
                this.checkNext = true;
            } else if (!this.f154it.hasNext()) {
                this.done = true;
                return null;
            }
            return (T) ObjectHelper.requireNonNull(this.f154it.next(), "The iterator returned a null value");
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return this.done;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            this.done = true;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.disposed = true;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }
    }
}
