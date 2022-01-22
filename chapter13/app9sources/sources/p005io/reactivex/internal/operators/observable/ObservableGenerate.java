package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.Callable;
import p005io.reactivex.Emitter;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableGenerate */
/* loaded from: classes.dex */
public final class ObservableGenerate<T, S> extends Observable<T> {
    final Consumer<? super S> disposeState;
    final BiFunction<S, Emitter<T>, S> generator;
    final Callable<S> stateSupplier;

    public ObservableGenerate(Callable<S> callable, BiFunction<S, Emitter<T>, S> biFunction, Consumer<? super S> consumer) {
        this.stateSupplier = callable;
        this.generator = biFunction;
        this.disposeState = consumer;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        try {
            GeneratorDisposable generatorDisposable = new GeneratorDisposable(observer, this.generator, this.disposeState, this.stateSupplier.call());
            observer.onSubscribe(generatorDisposable);
            generatorDisposable.run();
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptyDisposable.error(th, observer);
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableGenerate$GeneratorDisposable */
    /* loaded from: classes.dex */
    static final class GeneratorDisposable<T, S> implements Emitter<T>, Disposable {
        volatile boolean cancelled;
        final Consumer<? super S> disposeState;
        final Observer<? super T> downstream;
        final BiFunction<S, ? super Emitter<T>, S> generator;
        boolean hasNext;
        S state;
        boolean terminate;

        GeneratorDisposable(Observer<? super T> observer, BiFunction<S, ? super Emitter<T>, S> biFunction, Consumer<? super S> consumer, S s) {
            this.downstream = observer;
            this.generator = biFunction;
            this.disposeState = consumer;
            this.state = s;
        }

        public void run() {
            S s = this.state;
            if (this.cancelled) {
                this.state = null;
                dispose(s);
                return;
            }
            BiFunction<S, ? super Emitter<T>, S> biFunction = this.generator;
            while (!this.cancelled) {
                this.hasNext = false;
                try {
                    s = biFunction.apply(s, this);
                    if (this.terminate) {
                        this.cancelled = true;
                        this.state = null;
                        dispose(s);
                        return;
                    }
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    this.state = null;
                    this.cancelled = true;
                    onError(th);
                    dispose(s);
                    return;
                }
            }
            this.state = null;
            dispose(s);
        }

        private void dispose(S s) {
            try {
                this.disposeState.accept(s);
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                RxJavaPlugins.onError(th);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.cancelled = true;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        @Override // p005io.reactivex.Emitter
        public void onNext(T t) {
            if (this.terminate) {
                return;
            }
            if (this.hasNext) {
                onError(new IllegalStateException("onNext already called in this generate turn"));
            } else if (t == null) {
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
            } else {
                this.hasNext = true;
                this.downstream.onNext(t);
            }
        }

        @Override // p005io.reactivex.Emitter
        public void onError(Throwable th) {
            if (this.terminate) {
                RxJavaPlugins.onError(th);
                return;
            }
            if (th == null) {
                th = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            this.terminate = true;
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Emitter
        public void onComplete() {
            if (!this.terminate) {
                this.terminate = true;
                this.downstream.onComplete();
            }
        }
    }
}
