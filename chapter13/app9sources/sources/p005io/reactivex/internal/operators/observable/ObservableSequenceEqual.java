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

    public ObservableSequenceEqual(ObservableSource<? extends T> observableSource, ObservableSource<? extends T> observableSource2, BiPredicate<? super T, ? super T> biPredicate, int i) {
        this.first = observableSource;
        this.second = observableSource2;
        this.comparer = biPredicate;
        this.bufferSize = i;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Boolean> observer) {
        EqualCoordinator equalCoordinator = new EqualCoordinator(observer, this.bufferSize, this.first, this.second, this.comparer);
        observer.onSubscribe(equalCoordinator);
        equalCoordinator.subscribe();
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

        EqualCoordinator(Observer<? super Boolean> observer, int i, ObservableSource<? extends T> observableSource, ObservableSource<? extends T> observableSource2, BiPredicate<? super T, ? super T> biPredicate) {
            this.downstream = observer;
            this.first = observableSource;
            this.second = observableSource2;
            this.comparer = biPredicate;
            this.observers = r3;
            EqualObserver<T>[] equalObserverArr = {new EqualObserver<>(this, 0, i), new EqualObserver<>(this, 1, i)};
        }

        boolean setDisposable(Disposable disposable, int i) {
            return this.resources.setResource(i, disposable);
        }

        void subscribe() {
            EqualObserver<T>[] equalObserverArr = this.observers;
            this.first.subscribe(equalObserverArr[0]);
            this.second.subscribe(equalObserverArr[1]);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.resources.dispose();
                if (getAndIncrement() == 0) {
                    EqualObserver<T>[] equalObserverArr = this.observers;
                    equalObserverArr[0].queue.clear();
                    equalObserverArr[1].queue.clear();
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void cancel(SpscLinkedArrayQueue<T> spscLinkedArrayQueue, SpscLinkedArrayQueue<T> spscLinkedArrayQueue2) {
            this.cancelled = true;
            spscLinkedArrayQueue.clear();
            spscLinkedArrayQueue2.clear();
        }

        void drain() {
            Throwable th;
            Throwable th2;
            if (getAndIncrement() == 0) {
                EqualObserver<T>[] equalObserverArr = this.observers;
                EqualObserver<T> equalObserver = equalObserverArr[0];
                SpscLinkedArrayQueue<T> spscLinkedArrayQueue = equalObserver.queue;
                EqualObserver<T> equalObserver2 = equalObserverArr[1];
                SpscLinkedArrayQueue<T> spscLinkedArrayQueue2 = equalObserver2.queue;
                int i = 1;
                while (!this.cancelled) {
                    boolean z = equalObserver.done;
                    if (!z || (th2 = equalObserver.error) == null) {
                        boolean z2 = equalObserver2.done;
                        if (!z2 || (th = equalObserver2.error) == null) {
                            if (this.f159v1 == null) {
                                this.f159v1 = spscLinkedArrayQueue.poll();
                            }
                            boolean z3 = this.f159v1 == null;
                            if (this.f160v2 == null) {
                                this.f160v2 = spscLinkedArrayQueue2.poll();
                            }
                            boolean z4 = this.f160v2 == null;
                            if (z && z2 && z3 && z4) {
                                this.downstream.onNext(true);
                                this.downstream.onComplete();
                                return;
                            } else if (!z || !z2 || z3 == z4) {
                                if (!z3 && !z4) {
                                    try {
                                        if (!this.comparer.test((T) this.f159v1, (T) this.f160v2)) {
                                            cancel(spscLinkedArrayQueue, spscLinkedArrayQueue2);
                                            this.downstream.onNext(false);
                                            this.downstream.onComplete();
                                            return;
                                        }
                                        this.f159v1 = null;
                                        this.f160v2 = null;
                                    } catch (Throwable th3) {
                                        Exceptions.throwIfFatal(th3);
                                        cancel(spscLinkedArrayQueue, spscLinkedArrayQueue2);
                                        this.downstream.onError(th3);
                                        return;
                                    }
                                }
                                if (z3 || z4) {
                                    i = addAndGet(-i);
                                    if (i == 0) {
                                        return;
                                    }
                                }
                            } else {
                                cancel(spscLinkedArrayQueue, spscLinkedArrayQueue2);
                                this.downstream.onNext(false);
                                this.downstream.onComplete();
                                return;
                            }
                        } else {
                            cancel(spscLinkedArrayQueue, spscLinkedArrayQueue2);
                            this.downstream.onError(th);
                            return;
                        }
                    } else {
                        cancel(spscLinkedArrayQueue, spscLinkedArrayQueue2);
                        this.downstream.onError(th2);
                        return;
                    }
                }
                spscLinkedArrayQueue.clear();
                spscLinkedArrayQueue2.clear();
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

        EqualObserver(EqualCoordinator<T> equalCoordinator, int i, int i2) {
            this.parent = equalCoordinator;
            this.index = i;
            this.queue = new SpscLinkedArrayQueue<>(i2);
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            this.parent.setDisposable(disposable, this.index);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.queue.offer(t);
            this.parent.drain();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.error = th;
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
