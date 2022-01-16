package p005io.reactivex.internal.operators.observable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableBuffer */
/* loaded from: classes.dex */
public final class ObservableBuffer<T, U extends Collection<? super T>> extends AbstractObservableWithUpstream<T, U> {
    final Callable<U> bufferSupplier;
    final int count;
    final int skip;

    public ObservableBuffer(ObservableSource<T> source, int count, int skip, Callable<U> bufferSupplier) {
        super(source);
        this.count = count;
        this.skip = skip;
        this.bufferSupplier = bufferSupplier;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super U> t) {
        int i = this.skip;
        int i2 = this.count;
        if (i == i2) {
            BufferExactObserver<T, U> bes = new BufferExactObserver<>(t, i2, this.bufferSupplier);
            if (bes.createBuffer()) {
                this.source.subscribe(bes);
                return;
            }
            return;
        }
        this.source.subscribe(new BufferSkipObserver(t, this.count, this.skip, this.bufferSupplier));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableBuffer$BufferExactObserver */
    /* loaded from: classes.dex */
    static final class BufferExactObserver<T, U extends Collection<? super T>> implements Observer<T>, Disposable {
        U buffer;
        final Callable<U> bufferSupplier;
        final int count;
        final Observer<? super U> downstream;
        int size;
        Disposable upstream;

        BufferExactObserver(Observer<? super U> actual, int count, Callable<U> bufferSupplier) {
            this.downstream = actual;
            this.count = count;
            this.bufferSupplier = bufferSupplier;
        }

        boolean createBuffer() {
            try {
                this.buffer = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "Empty buffer supplied"));
                return true;
            } catch (Throwable t) {
                Exceptions.throwIfFatal(t);
                this.buffer = null;
                Disposable disposable = this.upstream;
                if (disposable == null) {
                    EmptyDisposable.error(t, this.downstream);
                    return false;
                }
                disposable.dispose();
                this.downstream.onError(t);
                return false;
            }
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

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            U b = this.buffer;
            if (b != null) {
                b.add(t);
                int i = this.size + 1;
                this.size = i;
                if (i >= this.count) {
                    this.downstream.onNext(b);
                    this.size = 0;
                    createBuffer();
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            this.buffer = null;
            this.downstream.onError(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            U b = this.buffer;
            if (b != null) {
                this.buffer = null;
                if (!b.isEmpty()) {
                    this.downstream.onNext(b);
                }
                this.downstream.onComplete();
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableBuffer$BufferSkipObserver */
    /* loaded from: classes.dex */
    static final class BufferSkipObserver<T, U extends Collection<? super T>> extends AtomicBoolean implements Observer<T>, Disposable {
        private static final long serialVersionUID = -8223395059921494546L;
        final Callable<U> bufferSupplier;
        final ArrayDeque<U> buffers = new ArrayDeque<>();
        final int count;
        final Observer<? super U> downstream;
        long index;
        final int skip;
        Disposable upstream;

        BufferSkipObserver(Observer<? super U> actual, int count, int skip, Callable<U> bufferSupplier) {
            this.downstream = actual;
            this.count = count;
            this.skip = skip;
            this.bufferSupplier = bufferSupplier;
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

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            long j = this.index;
            this.index = 1 + j;
            if (j % ((long) this.skip) == 0) {
                try {
                    this.buffers.offer((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The bufferSupplier returned a null collection. Null values are generally not allowed in 2.x operators and sources."));
                } catch (Throwable e) {
                    this.buffers.clear();
                    this.upstream.dispose();
                    this.downstream.onError(e);
                    return;
                }
            }
            Iterator<U> it = this.buffers.iterator();
            while (it.hasNext()) {
                U b = it.next();
                b.add(t);
                if (this.count <= b.size()) {
                    it.remove();
                    this.downstream.onNext(b);
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            this.buffers.clear();
            this.downstream.onError(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            while (!this.buffers.isEmpty()) {
                this.downstream.onNext(this.buffers.poll());
            }
            this.downstream.onComplete();
        }
    }
}
