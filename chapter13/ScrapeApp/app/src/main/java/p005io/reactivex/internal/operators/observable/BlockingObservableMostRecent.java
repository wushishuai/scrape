package p005io.reactivex.internal.operators.observable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.observers.DefaultObserver;

/* renamed from: io.reactivex.internal.operators.observable.BlockingObservableMostRecent */
/* loaded from: classes.dex */
public final class BlockingObservableMostRecent<T> implements Iterable<T> {
    final T initialValue;
    final ObservableSource<T> source;

    public BlockingObservableMostRecent(ObservableSource<T> source, T initialValue) {
        this.source = source;
        this.initialValue = initialValue;
    }

    @Override // java.lang.Iterable
    public Iterator<T> iterator() {
        MostRecentObserver<T> mostRecentObserver = new MostRecentObserver<>(this.initialValue);
        this.source.subscribe(mostRecentObserver);
        return mostRecentObserver.getIterable();
    }

    /* renamed from: io.reactivex.internal.operators.observable.BlockingObservableMostRecent$MostRecentObserver */
    /* loaded from: classes.dex */
    static final class MostRecentObserver<T> extends DefaultObserver<T> {
        volatile Object value;

        MostRecentObserver(T value) {
            this.value = NotificationLite.next(value);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.value = NotificationLite.complete();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            this.value = NotificationLite.error(e);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T args) {
            this.value = NotificationLite.next(args);
        }

        public MostRecentObserver<T>.Iterator getIterable() {
            return new Iterator();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.operators.observable.BlockingObservableMostRecent$MostRecentObserver$Iterator */
        /* loaded from: classes.dex */
        public final class Iterator implements java.util.Iterator<T> {
            private Object buf;

            Iterator() {
            }

            @Override // java.util.Iterator
            public boolean hasNext() {
                this.buf = MostRecentObserver.this.value;
                return !NotificationLite.isComplete(this.buf);
            }

            @Override // java.util.Iterator
            public T next() {
                try {
                    if (this.buf == null) {
                        this.buf = MostRecentObserver.this.value;
                    }
                    if (NotificationLite.isComplete(this.buf)) {
                        throw new NoSuchElementException();
                    } else if (!NotificationLite.isError(this.buf)) {
                        return (T) NotificationLite.getValue(this.buf);
                    } else {
                        throw ExceptionHelper.wrapOrThrow(NotificationLite.getError(this.buf));
                    }
                } finally {
                    this.buf = null;
                }
            }

            @Override // java.util.Iterator
            public void remove() {
                throw new UnsupportedOperationException("Read only iterator");
            }
        }
    }
}
