package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;

/* renamed from: io.reactivex.internal.operators.observable.ObservableTakeLastTimed */
/* loaded from: classes.dex */
public final class ObservableTakeLastTimed<T> extends AbstractObservableWithUpstream<T, T> {
    final int bufferSize;
    final long count;
    final boolean delayError;
    final Scheduler scheduler;
    final long time;
    final TimeUnit unit;

    public ObservableTakeLastTimed(ObservableSource<T> observableSource, long j, long j2, TimeUnit timeUnit, Scheduler scheduler, int i, boolean z) {
        super(observableSource);
        this.count = j;
        this.time = j2;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.bufferSize = i;
        this.delayError = z;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new TakeLastTimedObserver(observer, this.count, this.time, this.unit, this.scheduler, this.bufferSize, this.delayError));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableTakeLastTimed$TakeLastTimedObserver */
    /* loaded from: classes.dex */
    static final class TakeLastTimedObserver<T> extends AtomicBoolean implements Observer<T>, Disposable {
        private static final long serialVersionUID = -5677354903406201275L;
        volatile boolean cancelled;
        final long count;
        final boolean delayError;
        final Observer<? super T> downstream;
        Throwable error;
        final SpscLinkedArrayQueue<Object> queue;
        final Scheduler scheduler;
        final long time;
        final TimeUnit unit;
        Disposable upstream;

        TakeLastTimedObserver(Observer<? super T> observer, long j, long j2, TimeUnit timeUnit, Scheduler scheduler, int i, boolean z) {
            this.downstream = observer;
            this.count = j;
            this.time = j2;
            this.unit = timeUnit;
            this.scheduler = scheduler;
            this.queue = new SpscLinkedArrayQueue<>(i);
            this.delayError = z;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            SpscLinkedArrayQueue<Object> spscLinkedArrayQueue = this.queue;
            long now = this.scheduler.now(this.unit);
            long j = this.time;
            long j2 = this.count;
            boolean z = j2 == Long.MAX_VALUE;
            spscLinkedArrayQueue.offer(Long.valueOf(now), t);
            while (!spscLinkedArrayQueue.isEmpty()) {
                if (((Long) spscLinkedArrayQueue.peek()).longValue() <= now - j || (!z && ((long) (spscLinkedArrayQueue.size() >> 1)) > j2)) {
                    spscLinkedArrayQueue.poll();
                    spscLinkedArrayQueue.poll();
                } else {
                    return;
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.error = th;
            drain();
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            drain();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.upstream.dispose();
                if (compareAndSet(false, true)) {
                    this.queue.clear();
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void drain() {
            Throwable th;
            if (compareAndSet(false, true)) {
                Observer<? super T> observer = this.downstream;
                SpscLinkedArrayQueue<Object> spscLinkedArrayQueue = this.queue;
                boolean z = this.delayError;
                while (!this.cancelled) {
                    if (z || (th = this.error) == null) {
                        Object poll = spscLinkedArrayQueue.poll();
                        if (poll == null) {
                            Throwable th2 = this.error;
                            if (th2 != null) {
                                observer.onError(th2);
                                return;
                            } else {
                                observer.onComplete();
                                return;
                            }
                        } else {
                            Object poll2 = spscLinkedArrayQueue.poll();
                            if (((Long) poll).longValue() >= this.scheduler.now(this.unit) - this.time) {
                                observer.onNext(poll2);
                            }
                        }
                    } else {
                        spscLinkedArrayQueue.clear();
                        observer.onError(th);
                        return;
                    }
                }
                spscLinkedArrayQueue.clear();
            }
        }
    }
}
