package p005io.reactivex.internal.operators.flowable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Notification;
import p005io.reactivex.internal.util.BlockingHelper;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.subscribers.DisposableSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.BlockingFlowableLatest */
/* loaded from: classes.dex */
public final class BlockingFlowableLatest<T> implements Iterable<T> {
    final Publisher<? extends T> source;

    public BlockingFlowableLatest(Publisher<? extends T> publisher) {
        this.source = publisher;
    }

    @Override // java.lang.Iterable
    public Iterator<T> iterator() {
        LatestSubscriberIterator latestSubscriberIterator = new LatestSubscriberIterator();
        Flowable.fromPublisher(this.source).materialize().subscribe((FlowableSubscriber<? super Notification<T>>) latestSubscriberIterator);
        return latestSubscriberIterator;
    }

    /* renamed from: io.reactivex.internal.operators.flowable.BlockingFlowableLatest$LatestSubscriberIterator */
    /* loaded from: classes.dex */
    static final class LatestSubscriberIterator<T> extends DisposableSubscriber<Notification<T>> implements Iterator<T> {
        Notification<T> iteratorNotification;
        final Semaphore notify = new Semaphore(0);
        final AtomicReference<Notification<T>> value = new AtomicReference<>();

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
        }

        LatestSubscriberIterator() {
        }

        @Override // org.reactivestreams.Subscriber
        public /* bridge */ /* synthetic */ void onNext(Object obj) {
            onNext((Notification) ((Notification) obj));
        }

        public void onNext(Notification<T> notification) {
            if (this.value.getAndSet(notification) == null) {
                this.notify.release();
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            RxJavaPlugins.onError(th);
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            Notification<T> notification = this.iteratorNotification;
            if (notification == null || !notification.isOnError()) {
                Notification<T> notification2 = this.iteratorNotification;
                if ((notification2 == null || notification2.isOnNext()) && this.iteratorNotification == null) {
                    try {
                        BlockingHelper.verifyNonBlocking();
                        this.notify.acquire();
                        Notification<T> andSet = this.value.getAndSet(null);
                        this.iteratorNotification = andSet;
                        if (andSet.isOnError()) {
                            throw ExceptionHelper.wrapOrThrow(andSet.getError());
                        }
                    } catch (InterruptedException e) {
                        dispose();
                        this.iteratorNotification = Notification.createOnError(e);
                        throw ExceptionHelper.wrapOrThrow(e);
                    }
                }
                return this.iteratorNotification.isOnNext();
            }
            throw ExceptionHelper.wrapOrThrow(this.iteratorNotification.getError());
        }

        @Override // java.util.Iterator
        public T next() {
            if (!hasNext() || !this.iteratorNotification.isOnNext()) {
                throw new NoSuchElementException();
            }
            T value = this.iteratorNotification.getValue();
            this.iteratorNotification = null;
            return value;
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException("Read-only iterator.");
        }
    }
}
