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

    public ObservableRangeLong(long j, long j2) {
        this.start = j;
        this.count = j2;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super Long> observer) {
        long j = this.start;
        RangeDisposable rangeDisposable = new RangeDisposable(observer, j, j + this.count);
        observer.onSubscribe(rangeDisposable);
        rangeDisposable.run();
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableRangeLong$RangeDisposable */
    /* loaded from: classes.dex */
    static final class RangeDisposable extends BasicIntQueueDisposable<Long> {
        private static final long serialVersionUID = 396518478098735504L;
        final Observer<? super Long> downstream;
        final long end;
        boolean fused;
        long index;

        RangeDisposable(Observer<? super Long> observer, long j, long j2) {
            this.downstream = observer;
            this.index = j;
            this.end = j2;
        }

        void run() {
            if (!this.fused) {
                Observer<? super Long> observer = this.downstream;
                long j = this.end;
                for (long j2 = this.index; j2 != j && get() == 0; j2++) {
                    observer.onNext(Long.valueOf(j2));
                }
                if (get() == 0) {
                    lazySet(1);
                    observer.onComplete();
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public Long poll() throws Exception {
            long j = this.index;
            if (j != this.end) {
                this.index = 1 + j;
                return Long.valueOf(j);
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
        public int requestFusion(int i) {
            if ((i & 1) == 0) {
                return 0;
            }
            this.fused = true;
            return 1;
        }
    }
}
