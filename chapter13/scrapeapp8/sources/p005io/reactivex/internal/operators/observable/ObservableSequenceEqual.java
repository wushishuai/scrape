package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiPredicate;
import p005io.reactivex.internal.disposables.ArrayCompositeDisposable;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;

/* renamed from: io.reactivex.internal.operators.observable.ObservableSequenceEqual */
/* loaded from: classes.dex */
public final class ObservableSequenceEqual<T> extends Observable<Boolean> {
    final int bufferSize;
    final BiPredicate<? super T, ? super T> comparer;
    final ObservableSource<? extends T> first;
    final ObservableSource<? extends T> second;

    public ObservableSequenceEqual(ObservableSource<? extends T> first, ObservableSource<? extends T> second, BiPredicate<? super T, ? super T> comparer, int bufferSize) {
        this.first = first;
        this.second = second;
        this.comparer = comparer;
        this.bufferSize = bufferSize;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Boolean> observer) {
        EqualCoordinator<T> ec = new EqualCoordinator<>(observer, this.bufferSize, this.first, this.second, this.comparer);
        observer.onSubscribe(ec);
        ec.subscribe();
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSequenceEqual$EqualCoordinator */
    /* loaded from: classes.dex */
    static final class EqualCoordinator<T> extends AtomicInteger implements Disposable {
        private static final long serialVersionUID = -6178010334400373240L;
        volatile boolean cancelled;
        final BiPredicate<? super T, ? super T> comparer;
        final Observer<? super Boolean> downstream;
        final ObservableSource<? extends T> first;
        final EqualObserver<T>[] observers;
        final ArrayCompositeDisposable resources = new ArrayCompositeDisposable(2);
        final ObservableSource<? extends T> second;

        /* renamed from: v1 */
        T f159v1;

        /* renamed from: v2 */
        T f160v2;

        EqualCoordinator(Observer<? super Boolean> actual, int bufferSize, ObservableSource<? extends T> first, ObservableSource<? extends T> second, BiPredicate<? super T, ? super T> comparer) {
            this.downstream = actual;
            this.first = first;
            this.second = second;
            this.comparer = comparer;
            this.observers = as;
            EqualObserver<T>[] as = {new EqualObserver<>(this, 0, bufferSize), new EqualObserver<>(this, 1, bufferSize)};
        }

        boolean setDisposable(Disposable d, int index) {
            return this.resources.setResource(index, d);
        }

        void subscribe() {
            EqualObserver<T>[] as = this.observers;
            this.first.subscribe(as[0]);
            this.second.subscribe(as[1]);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.resources.dispose();
                if (getAndIncrement() == 0) {
                    EqualObserver<T>[] as = this.observers;
                    as[0].queue.clear();
                    as[1].queue.clear();
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void cancel(SpscLinkedArrayQueue<T> q1, SpscLinkedArrayQueue<T> q2) {
            this.cancelled = true;
            q1.clear();
            q2.clear();
        }

        /* JADX INFO: Multiple debug info for r9v0 boolean: [D('e' java.lang.Throwable), D('d2' boolean)] */
        void drain() {
            Throwable e;
            Throwable e2;
            if (getAndIncrement() == 0) {
                int missed = 1;
                EqualObserver<T>[] as = this.observers;
                EqualObserver<T> observer1 = as[0];
                SpscLinkedArrayQueue<T> q1 = observer1.queue;
                EqualObserver<T> observer2 = as[1];
                SpscLinkedArrayQueue<T> q2 = observer2.queue;
                while (!this.cancelled) {
                    boolean d1 = observer1.done;
                    if (!d1 || (e2 = observer1.error) == null) {
                        boolean d2 = observer2.done;
                        if (!d2 || (e = observer2.error) == null) {
                            if (this.f159v1 == null) {
                                this.f159v1 = q1.poll();
                            }
                            boolean e1 = this.f159v1 == null;
                            if (this.f160v2 == null) {
                                this.f160v2 = q2.poll();
                            }
                            boolean e22 = this.f160v2 == null;
                            if (d1 && d2 && e1 && e22) {
                                this.downstream.onNext(true);
                                this.downstream.onComplete();
                                return;
                            } else if (!d1 || !d2 || e1 == e22) {
                                if (!e1 && !e22) {
                                    try {
                                        if (!this.comparer.test((T) this.f159v1, (T) this.f160v2)) {
                                            cancel(q1, q2);
                                            this.downstream.onNext(false);
                                            this.downstream.onComplete();
                                            return;
                                        }
                                        this.f159v1 = null;
                                        this.f160v2 = null;
                                    } catch (Throwable ex) {
                                        Exceptions.throwIfFatal(ex);
                                        cancel(q1, q2);
                                        this.downstream.onError(ex);
                                        return;
                                    }
                                }
                                if (e1 || e22) {
                                    missed = addAndGet(-missed);
                                    if (missed == 0) {
                                        return;
                                    }
                                }
                            } else {
                                cancel(q1, q2);
                                this.downstream.onNext(false);
                                this.downstream.onComplete();
                                return;
                            }
                        } else {
                            cancel(q1, q2);
                            this.downstream.onError(e);
                            return;
                        }
                    } else {
                        cancel(q1, q2);
                        this.downstream.onError(e2);
                        return;
                    }
                }
                q1.clear();
                q2.clear();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableSequenceEqual$EqualObserver */
    /* loaded from: classes.dex */
    public static final class EqualObserver<T> implements Observer<T> {
        volatile boolean done;
        Throwable error;
        final int index;
        final EqualCoordinator<T> parent;
        final SpscLinkedArrayQueue<T> queue;

        EqualObserver(EqualCoordinator<T> parent, int index, int bufferSize) {
            this.parent = parent;
            this.index = index;
            this.queue = new SpscLinkedArrayQueue<>(bufferSize);
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            this.parent.setDisposable(d, this.index);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.queue.offer(t);
            this.parent.drain();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            this.error = t;
            this.done = true;
            this.parent.drain();
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.done = true;
            this.parent.drain();
        }
    }
}
