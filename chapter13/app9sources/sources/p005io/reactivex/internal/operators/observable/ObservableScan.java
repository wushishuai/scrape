package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableScan */
/* loaded from: classes.dex */
public final class ObservableScan<T> extends AbstractObservableWithUpstream<T, T> {
    final BiFunction<T, T, T> accumulator;

    public ObservableScan(ObservableSource<T> observableSource, BiFunction<T, T, T> biFunction) {
        super(observableSource);
        this.accumulator = biFunction;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new ScanObserver(observer, this.accumulator));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableScan$ScanObserver */
    /* loaded from: classes.dex */
    static final class ScanObserver<T> implements Observer<T>, Disposable {
        final BiFunction<T, T, T> accumulator;
        boolean done;
        final Observer<? super T> downstream;
        Disposable upstream;
        T value;

        ScanObserver(Observer<? super T> observer, BiFunction<T, T, T> biFunction) {
            this.downstream = observer;
            this.accumulator = biFunction;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream.isDisposed();
        }

        /* JADX WARN: Type inference failed for: r4v3, types: [T, java.lang.Object] */
        /* JADX WARN: Unknown variable types count: 1 */
        @Override // p005io.reactivex.Observer
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void onNext(T r4) {
            /*
                r3 = this;
                boolean r0 = r3.done
                if (r0 == 0) goto L_0x0005
                return
            L_0x0005:
                io.reactivex.Observer<? super T> r0 = r3.downstream
                T r1 = r3.value
                if (r1 != 0) goto L_0x0011
                r3.value = r4
                r0.onNext(r4)
                goto L_0x0022
            L_0x0011:
                io.reactivex.functions.BiFunction<T, T, T> r2 = r3.accumulator     // Catch: Throwable -> 0x0023
                java.lang.Object r4 = r2.apply(r1, r4)     // Catch: Throwable -> 0x0023
                java.lang.String r1 = "The value returned by the accumulator is null"
                java.lang.Object r4 = p005io.reactivex.internal.functions.ObjectHelper.requireNonNull(r4, r1)     // Catch: Throwable -> 0x0023
                r3.value = r4
                r0.onNext(r4)
            L_0x0022:
                return
            L_0x0023:
                r4 = move-exception
                p005io.reactivex.exceptions.Exceptions.throwIfFatal(r4)
                io.reactivex.disposables.Disposable r0 = r3.upstream
                r0.dispose()
                r3.onError(r4)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.observable.ObservableScan.ScanObserver.onNext(java.lang.Object):void");
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.downstream.onComplete();
            }
        }
    }
}
