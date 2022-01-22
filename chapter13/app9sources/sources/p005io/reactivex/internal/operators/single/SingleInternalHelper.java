package p005io.reactivex.internal.operators.single;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import org.reactivestreams.Publisher;
import p005io.reactivex.Flowable;
import p005io.reactivex.Observable;
import p005io.reactivex.SingleSource;
import p005io.reactivex.functions.Function;

/* renamed from: io.reactivex.internal.operators.single.SingleInternalHelper */
/* loaded from: classes.dex */
public final class SingleInternalHelper {
    private SingleInternalHelper() {
        throw new IllegalStateException("No instances!");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.single.SingleInternalHelper$NoSuchElementCallable */
    /* loaded from: classes.dex */
    public enum NoSuchElementCallable implements Callable<NoSuchElementException> {
        INSTANCE;

        @Override // java.util.concurrent.Callable
        public NoSuchElementException call() throws Exception {
            return new NoSuchElementException();
        }
    }

    public static <T> Callable<NoSuchElementException> emptyThrower() {
        return NoSuchElementCallable.INSTANCE;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.single.SingleInternalHelper$ToFlowable */
    /* loaded from: classes.dex */
    public enum ToFlowable implements Function<SingleSource, Publisher> {
        INSTANCE;

        public Publisher apply(SingleSource singleSource) {
            return new SingleToFlowable(singleSource);
        }
    }

    public static <T> Function<SingleSource<? extends T>, Publisher<? extends T>> toFlowable() {
        return ToFlowable.INSTANCE;
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleInternalHelper$ToFlowableIterator */
    /* loaded from: classes.dex */
    static final class ToFlowableIterator<T> implements Iterator<Flowable<T>> {
        private final Iterator<? extends SingleSource<? extends T>> sit;

        ToFlowableIterator(Iterator<? extends SingleSource<? extends T>> it) {
            this.sit = it;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.sit.hasNext();
        }

        @Override // java.util.Iterator
        public Flowable<T> next() {
            return new SingleToFlowable((SingleSource) this.sit.next());
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleInternalHelper$ToFlowableIterable */
    /* loaded from: classes.dex */
    static final class ToFlowableIterable<T> implements Iterable<Flowable<T>> {
        private final Iterable<? extends SingleSource<? extends T>> sources;

        ToFlowableIterable(Iterable<? extends SingleSource<? extends T>> iterable) {
            this.sources = iterable;
        }

        @Override // java.lang.Iterable
        public Iterator<Flowable<T>> iterator() {
            return new ToFlowableIterator(this.sources.iterator());
        }
    }

    public static <T> Iterable<? extends Flowable<T>> iterableToFlowable(Iterable<? extends SingleSource<? extends T>> iterable) {
        return new ToFlowableIterable(iterable);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.single.SingleInternalHelper$ToObservable */
    /* loaded from: classes.dex */
    public enum ToObservable implements Function<SingleSource, Observable> {
        INSTANCE;

        public Observable apply(SingleSource singleSource) {
            return new SingleToObservable(singleSource);
        }
    }

    public static <T> Function<SingleSource<? extends T>, Observable<? extends T>> toObservable() {
        return ToObservable.INSTANCE;
    }
}
