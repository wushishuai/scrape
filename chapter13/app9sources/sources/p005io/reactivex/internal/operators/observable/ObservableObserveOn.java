package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.fuseable.QueueDisposable;
import p005io.reactivex.internal.fuseable.SimpleQueue;
import p005io.reactivex.internal.observers.BasicIntQueueDisposable;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;
import p005io.reactivex.internal.schedulers.TrampolineScheduler;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableObserveOn */
/* loaded from: classes.dex */
public final class ObservableObserveOn<T> extends AbstractObservableWithUpstream<T, T> {
    final int bufferSize;
    final boolean delayError;
    final Scheduler scheduler;

    public ObservableObserveOn(ObservableSource<T> observableSource, Scheduler scheduler, boolean z, int i) {
        super(observableSource);
        this.scheduler = scheduler;
        this.delayError = z;
        this.bufferSize = i;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        Scheduler scheduler = this.scheduler;
        if (scheduler instanceof TrampolineScheduler) {
            this.source.subscribe(observer);
            return;
        }
        this.source.subscribe(new ObserveOnObserver(observer, scheduler.createWorker(), this.delayError, this.bufferSize));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableObserveOn$ObserveOnObserver */
    /* loaded from: classes.dex */
    static final class ObserveOnObserver<T> extends BasicIntQueueDisposable<T> implements Observer<T>, Runnable {
        private static final long serialVersionUID = 6576896619930983584L;
        final int bufferSize;
        final boolean delayError;
        volatile boolean disposed;
        volatile boolean done;
        final Observer<? super T> downstream;
        Throwable error;
        boolean outputFused;
        SimpleQueue<T> queue;
        int sourceMode;
        Disposable upstream;
        final Scheduler.Worker worker;

        ObserveOnObserver(Observer<? super T> observer, Scheduler.Worker worker, boolean z, int i) {
            this.downstream = observer;
            this.worker = worker;
            this.delayError = z;
            this.bufferSize = i;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                if (disposable instanceof QueueDisposable) {
                    QueueDisposable queueDisposable = (QueueDisposable) disposable;
                    int requestFusion = queueDisposable.requestFusion(7);
                    if (requestFusion == 1) {
                        this.sourceMode = requestFusion;
                        this.queue = queueDisposable;
                        this.done = true;
                        this.downstream.onSubscribe(this);
                        schedule();
                        return;
                    } else if (requestFusion == 2) {
                        this.sourceMode = requestFusion;
                        this.queue = queueDisposable;
                        this.downstream.onSubscribe(this);
                        return;
                    }
                }
                this.queue = new SpscLinkedArrayQueue(this.bufferSize);
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            if (!this.done) {
                if (this.sourceMode != 2) {
                    this.queue.offer(t);
                }
                schedule();
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.error = th;
            this.done = true;
            schedule();
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                schedule();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.disposed) {
                this.disposed = true;
                this.upstream.dispose();
                this.worker.dispose();
                if (getAndIncrement() == 0) {
                    this.queue.clear();
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }

        void schedule() {
            if (getAndIncrement() == 0) {
                this.worker.schedule(this);
            }
        }

        void drainNormal() {
            SimpleQueue<T> simpleQueue = this.queue;
            Observer<? super T> observer = this.downstream;
            int i = 1;
            while (!checkTerminated(this.done, simpleQueue.isEmpty(), observer)) {
                while (true) {
                    boolean z = this.done;
                    try {
                        Object obj = (T) simpleQueue.poll();
                        boolean z2 = obj == null;
                        if (!checkTerminated(z, z2, observer)) {
                            if (z2) {
                                i = addAndGet(-i);
                                if (i == 0) {
                                    return;
                                }
                            } else {
                                observer.onNext(obj);
                            }
                        } else {
                            return;
                        }
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        this.disposed = true;
                        this.upstream.dispose();
                        simpleQueue.clear();
                        observer.onError(th);
                        this.worker.dispose();
                        return;
                    }
                }
            }
        }

        void drainFused() {
            int i = 1;
            while (!this.disposed) {
                boolean z = this.done;
                Throwable th = this.error;
                if (this.delayError || !z || th == null) {
                    this.downstream.onNext(null);
                    if (z) {
                        this.disposed = true;
                        Throwable th2 = this.error;
                        if (th2 != null) {
                            this.downstream.onError(th2);
                        } else {
                            this.downstream.onComplete();
                        }
                        this.worker.dispose();
                        return;
                    }
                    i = addAndGet(-i);
                    if (i == 0) {
                        return;
                    }
                } else {
                    this.disposed = true;
                    this.downstream.onError(th);
                    this.worker.dispose();
                    return;
                }
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.outputFused) {
                drainFused();
            } else {
                drainNormal();
            }
        }

        boolean checkTerminated(boolean z, boolean z2, Observer<? super T> observer) {
            if (this.disposed) {
                this.queue.clear();
                return true;
            } else if (!z) {
                return false;
            } else {
                Throwable th = this.error;
                if (this.delayError) {
                    if (!z2) {
                        return false;
                    }
                    this.disposed = true;
                    if (th != null) {
                        observer.onError(th);
                    } else {
                        observer.onComplete();
                    }
                    this.worker.dispose();
                    return true;
                } else if (th != null) {
                    this.disposed = true;
                    this.queue.clear();
                    observer.onError(th);
                    this.worker.dispose();
                    return true;
                } else if (!z2) {
                    return false;
                } else {
                    this.disposed = true;
                    observer.onComplete();
                    this.worker.dispose();
                    return true;
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            if ((i & 2) == 0) {
                return 0;
            }
            this.outputFused = true;
            return 2;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            return this.queue.poll();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            this.queue.clear();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return this.queue.isEmpty();
        }
    }
}
