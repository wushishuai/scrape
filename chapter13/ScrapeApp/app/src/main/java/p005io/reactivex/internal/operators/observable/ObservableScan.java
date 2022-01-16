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

    public ObservableScan(ObservableSource<T> source, BiFunction<T, T, T> accumulator) {
        super(source);
        this.accumulator = accumulator;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> t) {
        this.source.subscribe(new ScanObserver(t, this.accumulator));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableScan$ScanObserver */
    /* loaded from: classes.dex */
    static final class ScanObserver<T> implements Observer<T>, Disposable {
        final BiFunction<T, T, T> accumulator;
        boolean done;
        final Observer<? super T> downstream;
        Disposable upstream;
        T value;

        ScanObserver(Observer<? super T> actual, BiFunction<T, T, T> accumulator) {
            this.downstream = actual;
            this.accumulator = accumulator;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
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

        /* JADX WARN: Type inference failed for: r2v3, types: [T, java.lang.Object] */
        /* JADX WARN: Unknown variable types count: 1 */
        @Override // p005io.reactivex.Observer
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void onNext(T r5) {
            /*
                r4 = this;
                boolean r0 = r4.done
                if (r0 == 0) goto L_0x0005
                return
            L_0x0005:
                io.reactivex.Observer<? super T> r0 = r4.downstream
                T r1 = r4.value
                if (r1 != 0) goto L_0x0011
                r4.value = r5
                r0.onNext(r5)
                goto L_0x0023
            L_0x0011:
                io.reactivex.functions.BiFunction<T, T, T> r2 = r4.accumulator     // Catch: Throwable -> 0x0024
                java.lang.Object r2 = r2.apply(r1, r5)     // Catch: Throwable -> 0x0024
                java.lang.String r3 = "The value returned by the accumulator is null"
                java.lang.Object r2 = p005io.reactivex.internal.functions.ObjectHelper.requireNonNull(r2, r3)     // Catch: Throwable -> 0x0024
                r4.value = r2
                r0.onNext(r2)
            L_0x0023:
                return
            L_0x0024:
                r2 = move-exception
                p005io.reactivex.exceptions.Exceptions.throwIfFatal(r2)
                io.reactivex.disposables.Disposable r3 = r4.upstream
                r3.dispose()
                r4.onError(r2)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.observable.ObservableScan.ScanObserver.onNext(java.lang.Object):void");
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.done = true;
            this.downstream.onError(t);
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
