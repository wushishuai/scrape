package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.observers.BasicIntQueueDisposable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableRangeLong */
/* loaded from: classes.dex */
public final class ObservableRangeLong extends Observable<Long> {
    private final long count;
    private final long start;

    public ObservableRangeLong(long start, long count) {
        this.start = start;
        this.count = count;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super Long> o) {
        long j = this.start;
        RangeDisposable parent = new RangeDisposable(o, j, j + this.count);
        o.onSubscribe(parent);
        parent.run();
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableRangeLong$RangeDisposable */
    /* loaded from: classes.dex */
    static final class RangeDisposable extends BasicIntQueueDisposable<Long> {
        private static final long serialVersionUID = 396518478098735504L;
        final Observer<? super Long> downstream;
        final long end;
        boolean fused;
        long index;

        RangeDisposable(Observer<? super Long> actual, long start, long end) {
            this.downstream = actual;
            this.index = start;
            this.end = end;
        }

        void run() {
            if (!this.fused) {
                Observer<? super Long> actual = this.downstream;
                long e = this.end;
                for (long i = this.index; i != e && get() == 0; i++) {
                    actual.onNext(Long.valueOf(i));
                }
                if (get() == 0) {
                    lazySet(1);
                    actual.onComplete();
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public Long poll() throws Exception {
            long i = this.index;
            if (i != this.end) {
                this.index = 1 + i;
                return Long.valueOf(i);
            }
            lazySet(1);
            return null;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return this.index == this.end;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            this.index = this.end;
            lazySet(1);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            set(1);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get() != 0;
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            if ((mode & 1) == 0) {
                return 0;
            }
            this.fused = true;
            return 1;
        }
    }
}
