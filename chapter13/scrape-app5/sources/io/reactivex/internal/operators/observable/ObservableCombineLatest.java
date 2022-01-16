package io.reactivex.internal.operators.observable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.disposables.EmptyDisposable;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.queue.SpscLinkedArrayQueue;
import io.reactivex.internal.util.AtomicThrowable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
/* loaded from: classes.dex */
public final class ObservableCombineLatest<T, R> extends Observable<R> {
    final int bufferSize;
    final Function<? super Object[], ? extends R> combiner;
    final boolean delayError;
    final ObservableSource<? extends T>[] sources;
    final Iterable<? extends ObservableSource<? extends T>> sourcesIterable;

    public ObservableCombineLatest(ObservableSource<? extends T>[] sources, Iterable<? extends ObservableSource<? extends T>> sourcesIterable, Function<? super Object[], ? extends R> combiner, int bufferSize, boolean delayError) {
        this.sources = sources;
        this.sourcesIterable = sourcesIterable;
        this.combiner = combiner;
        this.bufferSize = bufferSize;
        this.delayError = delayError;
    }

    /* JADX INFO: Multiple debug info for r4v2 int: [D('count' int), D('b' io.reactivex.ObservableSource<? extends T>[])] */
    @Override // io.reactivex.Observable
    public void subscribeActual(Observer<? super R> observer) {
        ObservableSource<? extends T>[] sources = this.sources;
        int count = 0;
        if (sources == null) {
            sources = new Observable[8];
            for (ObservableSource<? extends T> p : this.sourcesIterable) {
                if (count == sources.length) {
                    ObservableSource<? extends T>[] b = new ObservableSource[(count >> 2) + count];
                    System.arraycopy(sources, 0, b, 0, count);
                    sources = b;
                }
                sources[count] = p;
                count++;
            }
        } else {
            count = sources.length;
        }
        if (count == 0) {
            EmptyDisposable.complete(observer);
        } else {
            new LatestCoordinator<>(observer, this.combiner, count, this.bufferSize, this.delayError).subscribe(sources);
        }
    }

    /* loaded from: classes.dex */
    static final class LatestCoordinator<T, R> extends AtomicInteger implements Disposable {
        private static final long serialVersionUID = 8567835998786448817L;
        int active;
        volatile boolean cancelled;
        final Function<? super Object[], ? extends R> combiner;
        int complete;
        final boolean delayError;
        volatile boolean done;
        final Observer<? super R> downstream;
        final AtomicThrowable errors = new AtomicThrowable();
        Object[] latest;
        final CombinerObserver<T, R>[] observers;
        final SpscLinkedArrayQueue<Object[]> queue;

        LatestCoordinator(Observer<? super R> actual, Function<? super Object[], ? extends R> combiner, int count, int bufferSize, boolean delayError) {
            this.downstream = actual;
            this.combiner = combiner;
            this.delayError = delayError;
            this.latest = new Object[count];
            CombinerObserver<T, R>[] as = new CombinerObserver[count];
            for (int i = 0; i < count; i++) {
                as[i] = new CombinerObserver<>(this, i);
            }
            this.observers = as;
            this.queue = new SpscLinkedArrayQueue<>(bufferSize);
        }

        public void subscribe(ObservableSource<? extends T>[] sources) {
            CombinerObserver<T, R>[] combinerObserverArr = this.observers;
            int len = combinerObserverArr.length;
            this.downstream.onSubscribe(this);
            for (int i = 0; i < len && !this.done && !this.cancelled; i++) {
                sources[i].subscribe(combinerObserverArr[i]);
            }
        }

        @Override // io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                cancelSources();
                if (getAndIncrement() == 0) {
                    clear(this.queue);
                }
            }
        }

        @Override // io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void cancelSources() {
            for (CombinerObserver<T, R> observer : this.observers) {
                observer.dispose();
            }
        }

        void clear(SpscLinkedArrayQueue<?> q) {
            synchronized (this) {
                this.latest = null;
            }
            q.clear();
        }

        void drain() {
            if (getAndIncrement() == 0) {
                SpscLinkedArrayQueue<Object[]> q = this.queue;
                Observer<? super R> a = this.downstream;
                boolean delayError = this.delayError;
                int missed = 1;
                while (!this.cancelled) {
                    if (delayError || this.errors.get() == null) {
                        boolean d = this.done;
                        Object[] s = q.poll();
                        boolean empty = s == null;
                        if (d && empty) {
                            clear(q);
                            Throwable ex = this.errors.terminate();
                            if (ex == null) {
                                a.onComplete();
                                return;
                            } else {
                                a.onError(ex);
                                return;
                            }
                        } else if (empty) {
                            missed = addAndGet(-missed);
                            if (missed == 0) {
                                return;
                            }
                        } else {
                            try {
                                a.onNext((Object) ObjectHelper.requireNonNull(this.combiner.apply(s), "The combiner returned a null value"));
                            } catch (Throwable ex2) {
                                Exceptions.throwIfFatal(ex2);
                                this.errors.addThrowable(ex2);
                                cancelSources();
                                clear(q);
                                a.onError(this.errors.terminate());
                                return;
                            }
                        }
                    } else {
                        cancelSources();
                        clear(q);
                        a.onError(this.errors.terminate());
                        return;
                    }
                }
                clear(q);
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        void innerNext(int index, T item) {
            boolean shouldDrain = false;
            synchronized (this) {
                Object[] latest = this.latest;
                if (latest != null) {
                    Object o = latest[index];
                    int a = this.active;
                    if (o == null) {
                        a++;
                        this.active = a;
                    }
                    latest[index] = item;
                    if (a == latest.length) {
                        this.queue.offer(latest.clone());
                        shouldDrain = true;
                    }
                    if (shouldDrain) {
                        drain();
                    }
                }
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:18:0x0025, code lost:
            if (r2 == r1.length) goto L_0x0027;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        void innerError(int r6, java.lang.Throwable r7) {
            /*
                r5 = this;
                io.reactivex.internal.util.AtomicThrowable r0 = r5.errors
                boolean r0 = r0.addThrowable(r7)
                if (r0 == 0) goto L_0x0037
                r0 = 1
                boolean r1 = r5.delayError
                if (r1 == 0) goto L_0x002e
                monitor-enter(r5)
                java.lang.Object[] r1 = r5.latest     // Catch: all -> 0x002b
                if (r1 != 0) goto L_0x0014
                monitor-exit(r5)     // Catch: all -> 0x002b
                return
            L_0x0014:
                r2 = r1[r6]     // Catch: all -> 0x002b
                r3 = 1
                if (r2 != 0) goto L_0x001b
                r2 = 1
                goto L_0x001c
            L_0x001b:
                r2 = 0
            L_0x001c:
                r0 = r2
                if (r0 != 0) goto L_0x0027
                int r2 = r5.complete     // Catch: all -> 0x002b
                int r2 = r2 + r3
                r5.complete = r2     // Catch: all -> 0x002b
                int r4 = r1.length     // Catch: all -> 0x002b
                if (r2 != r4) goto L_0x0029
            L_0x0027:
                r5.done = r3     // Catch: all -> 0x002b
            L_0x0029:
                monitor-exit(r5)     // Catch: all -> 0x002b
                goto L_0x002e
            L_0x002b:
                r1 = move-exception
                monitor-exit(r5)     // Catch: all -> 0x002b
                throw r1
            L_0x002e:
                if (r0 == 0) goto L_0x0033
                r5.cancelSources()
            L_0x0033:
                r5.drain()
                goto L_0x003a
            L_0x0037:
                io.reactivex.plugins.RxJavaPlugins.onError(r7)
            L_0x003a:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: io.reactivex.internal.operators.observable.ObservableCombineLatest.LatestCoordinator.innerError(int, java.lang.Throwable):void");
        }

        /* JADX WARN: Code restructure failed: missing block: B:15:0x0019, code lost:
            if (r2 == r1.length) goto L_0x001b;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        void innerComplete(int r6) {
            /*
                r5 = this;
                r0 = 0
                monitor-enter(r5)
                java.lang.Object[] r1 = r5.latest     // Catch: all -> 0x0027
                if (r1 != 0) goto L_0x0008
                monitor-exit(r5)     // Catch: all -> 0x0027
                return
            L_0x0008:
                r2 = r1[r6]     // Catch: all -> 0x0027
                r3 = 1
                if (r2 != 0) goto L_0x000f
                r2 = 1
                goto L_0x0010
            L_0x000f:
                r2 = 0
            L_0x0010:
                r0 = r2
                if (r0 != 0) goto L_0x001b
                int r2 = r5.complete     // Catch: all -> 0x0027
                int r2 = r2 + r3
                r5.complete = r2     // Catch: all -> 0x0027
                int r4 = r1.length     // Catch: all -> 0x0027
                if (r2 != r4) goto L_0x001d
            L_0x001b:
                r5.done = r3     // Catch: all -> 0x0027
            L_0x001d:
                monitor-exit(r5)     // Catch: all -> 0x0027
                if (r0 == 0) goto L_0x0023
                r5.cancelSources()
            L_0x0023:
                r5.drain()
                return
            L_0x0027:
                r1 = move-exception
                monitor-exit(r5)     // Catch: all -> 0x0027
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: io.reactivex.internal.operators.observable.ObservableCombineLatest.LatestCoordinator.innerComplete(int):void");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class CombinerObserver<T, R> extends AtomicReference<Disposable> implements Observer<T> {
        private static final long serialVersionUID = -4823716997131257941L;
        final int index;
        final LatestCoordinator<T, R> parent;

        CombinerObserver(LatestCoordinator<T, R> parent, int index) {
            this.parent = parent;
            this.index = index;
        }

        @Override // io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override // io.reactivex.Observer
        public void onNext(T t) {
            this.parent.innerNext(this.index, t);
        }

        @Override // io.reactivex.Observer
        public void onError(Throwable t) {
            this.parent.innerError(this.index, t);
        }

        @Override // io.reactivex.Observer
        public void onComplete() {
            this.parent.innerComplete(this.index);
        }

        public void dispose() {
            DisposableHelper.dispose(this);
        }
    }
}
