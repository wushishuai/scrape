package p005io.reactivex.internal.operators.observable;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import p005io.reactivex.Emitter;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.Scheduler;
import p005io.reactivex.functions.Action;
import p005io.reactivex.functions.BiConsumer;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.Functions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.observables.ConnectableObservable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper */
/* loaded from: classes.dex */
public final class ObservableInternalHelper {
    private ObservableInternalHelper() {
        throw new IllegalStateException("No instances!");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$SimpleGenerator */
    /* loaded from: classes.dex */
    public static final class SimpleGenerator<T, S> implements BiFunction<S, Emitter<T>, S> {
        final Consumer<Emitter<T>> consumer;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.functions.BiFunction
        public /* bridge */ /* synthetic */ Object apply(Object obj, Object obj2) throws Exception {
            return apply((SimpleGenerator<T, S>) obj, (Emitter) ((Emitter) obj2));
        }

        SimpleGenerator(Consumer<Emitter<T>> consumer) {
            this.consumer = consumer;
        }

        public S apply(S s, Emitter<T> emitter) throws Exception {
            this.consumer.accept(emitter);
            return s;
        }
    }

    public static <T, S> BiFunction<S, Emitter<T>, S> simpleGenerator(Consumer<Emitter<T>> consumer) {
        return new SimpleGenerator(consumer);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$SimpleBiGenerator */
    /* loaded from: classes.dex */
    public static final class SimpleBiGenerator<T, S> implements BiFunction<S, Emitter<T>, S> {
        final BiConsumer<S, Emitter<T>> consumer;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.functions.BiFunction
        public /* bridge */ /* synthetic */ Object apply(Object obj, Object obj2) throws Exception {
            return apply((SimpleBiGenerator<T, S>) obj, (Emitter) ((Emitter) obj2));
        }

        SimpleBiGenerator(BiConsumer<S, Emitter<T>> biConsumer) {
            this.consumer = biConsumer;
        }

        public S apply(S s, Emitter<T> emitter) throws Exception {
            this.consumer.accept(s, emitter);
            return s;
        }
    }

    public static <T, S> BiFunction<S, Emitter<T>, S> simpleBiGenerator(BiConsumer<S, Emitter<T>> biConsumer) {
        return new SimpleBiGenerator(biConsumer);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$ItemDelayFunction */
    /* loaded from: classes.dex */
    public static final class ItemDelayFunction<T, U> implements Function<T, ObservableSource<T>> {
        final Function<? super T, ? extends ObservableSource<U>> itemDelay;

        ItemDelayFunction(Function<? super T, ? extends ObservableSource<U>> function) {
            this.itemDelay = function;
        }

        @Override // p005io.reactivex.functions.Function
        public ObservableSource<T> apply(T t) throws Exception {
            return new ObservableTake((ObservableSource) ObjectHelper.requireNonNull(this.itemDelay.apply(t), "The itemDelay returned a null ObservableSource"), 1).map(Functions.justFunction(t)).defaultIfEmpty(t);
        }
    }

    public static <T, U> Function<T, ObservableSource<T>> itemDelay(Function<? super T, ? extends ObservableSource<U>> function) {
        return new ItemDelayFunction(function);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$ObserverOnNext */
    /* loaded from: classes.dex */
    public static final class ObserverOnNext<T> implements Consumer<T> {
        final Observer<T> observer;

        ObserverOnNext(Observer<T> observer) {
            this.observer = observer;
        }

        @Override // p005io.reactivex.functions.Consumer
        public void accept(T t) throws Exception {
            this.observer.onNext(t);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$ObserverOnError */
    /* loaded from: classes.dex */
    public static final class ObserverOnError<T> implements Consumer<Throwable> {
        final Observer<T> observer;

        ObserverOnError(Observer<T> observer) {
            this.observer = observer;
        }

        public void accept(Throwable th) throws Exception {
            this.observer.onError(th);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$ObserverOnComplete */
    /* loaded from: classes.dex */
    public static final class ObserverOnComplete<T> implements Action {
        final Observer<T> observer;

        ObserverOnComplete(Observer<T> observer) {
            this.observer = observer;
        }

        @Override // p005io.reactivex.functions.Action
        public void run() throws Exception {
            this.observer.onComplete();
        }
    }

    public static <T> Consumer<T> observerOnNext(Observer<T> observer) {
        return new ObserverOnNext(observer);
    }

    public static <T> Consumer<Throwable> observerOnError(Observer<T> observer) {
        return new ObserverOnError(observer);
    }

    public static <T> Action observerOnComplete(Observer<T> observer) {
        return new ObserverOnComplete(observer);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$FlatMapWithCombinerInner */
    /* loaded from: classes.dex */
    public static final class FlatMapWithCombinerInner<U, R, T> implements Function<U, R> {
        private final BiFunction<? super T, ? super U, ? extends R> combiner;

        /* renamed from: t */
        private final T f155t;

        FlatMapWithCombinerInner(BiFunction<? super T, ? super U, ? extends R> biFunction, T t) {
            this.combiner = biFunction;
            this.f155t = t;
        }

        @Override // p005io.reactivex.functions.Function
        public R apply(U u) throws Exception {
            return (R) this.combiner.apply((T) this.f155t, u);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$FlatMapWithCombinerOuter */
    /* loaded from: classes.dex */
    public static final class FlatMapWithCombinerOuter<T, R, U> implements Function<T, ObservableSource<R>> {
        private final BiFunction<? super T, ? super U, ? extends R> combiner;
        private final Function<? super T, ? extends ObservableSource<? extends U>> mapper;

        FlatMapWithCombinerOuter(BiFunction<? super T, ? super U, ? extends R> biFunction, Function<? super T, ? extends ObservableSource<? extends U>> function) {
            this.combiner = biFunction;
            this.mapper = function;
        }

        @Override // p005io.reactivex.functions.Function
        public ObservableSource<R> apply(T t) throws Exception {
            return new ObservableMap((ObservableSource) ObjectHelper.requireNonNull(this.mapper.apply(t), "The mapper returned a null ObservableSource"), new FlatMapWithCombinerInner(this.combiner, t));
        }
    }

    public static <T, U, R> Function<T, ObservableSource<R>> flatMapWithCombiner(Function<? super T, ? extends ObservableSource<? extends U>> function, BiFunction<? super T, ? super U, ? extends R> biFunction) {
        return new FlatMapWithCombinerOuter(biFunction, function);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$FlatMapIntoIterable */
    /* loaded from: classes.dex */
    public static final class FlatMapIntoIterable<T, U> implements Function<T, ObservableSource<U>> {
        private final Function<? super T, ? extends Iterable<? extends U>> mapper;

        FlatMapIntoIterable(Function<? super T, ? extends Iterable<? extends U>> function) {
            this.mapper = function;
        }

        @Override // p005io.reactivex.functions.Function
        public ObservableSource<U> apply(T t) throws Exception {
            return new ObservableFromIterable((Iterable) ObjectHelper.requireNonNull(this.mapper.apply(t), "The mapper returned a null Iterable"));
        }
    }

    public static <T, U> Function<T, ObservableSource<U>> flatMapIntoIterable(Function<? super T, ? extends Iterable<? extends U>> function) {
        return new FlatMapIntoIterable(function);
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$MapToInt */
    /* loaded from: classes.dex */
    enum MapToInt implements Function<Object, Object> {
        INSTANCE;

        @Override // p005io.reactivex.functions.Function
        public Object apply(Object obj) throws Exception {
            return 0;
        }
    }

    public static <T> Callable<ConnectableObservable<T>> replayCallable(Observable<T> observable) {
        return new ReplayCallable(observable);
    }

    public static <T> Callable<ConnectableObservable<T>> replayCallable(Observable<T> observable, int i) {
        return new BufferedReplayCallable(observable, i);
    }

    public static <T> Callable<ConnectableObservable<T>> replayCallable(Observable<T> observable, int i, long j, TimeUnit timeUnit, Scheduler scheduler) {
        return new BufferedTimedReplayCallable(observable, i, j, timeUnit, scheduler);
    }

    public static <T> Callable<ConnectableObservable<T>> replayCallable(Observable<T> observable, long j, TimeUnit timeUnit, Scheduler scheduler) {
        return new TimedReplayCallable(observable, j, timeUnit, scheduler);
    }

    public static <T, R> Function<Observable<T>, ObservableSource<R>> replayFunction(Function<? super Observable<T>, ? extends ObservableSource<R>> function, Scheduler scheduler) {
        return new ReplayFunction(function, scheduler);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$ZipIterableFunction */
    /* loaded from: classes.dex */
    public static final class ZipIterableFunction<T, R> implements Function<List<ObservableSource<? extends T>>, ObservableSource<? extends R>> {
        private final Function<? super Object[], ? extends R> zipper;

        @Override // p005io.reactivex.functions.Function
        public /* bridge */ /* synthetic */ Object apply(Object obj) throws Exception {
            return apply((List) ((List) obj));
        }

        ZipIterableFunction(Function<? super Object[], ? extends R> function) {
            this.zipper = function;
        }

        public ObservableSource<? extends R> apply(List<ObservableSource<? extends T>> list) {
            return Observable.zipIterable(list, this.zipper, false, Observable.bufferSize());
        }
    }

    public static <T, R> Function<List<ObservableSource<? extends T>>, ObservableSource<? extends R>> zipIterable(Function<? super Object[], ? extends R> function) {
        return new ZipIterableFunction(function);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$ReplayCallable */
    /* loaded from: classes.dex */
    public static final class ReplayCallable<T> implements Callable<ConnectableObservable<T>> {
        private final Observable<T> parent;

        ReplayCallable(Observable<T> observable) {
            this.parent = observable;
        }

        @Override // java.util.concurrent.Callable
        public ConnectableObservable<T> call() {
            return this.parent.replay();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$BufferedReplayCallable */
    /* loaded from: classes.dex */
    public static final class BufferedReplayCallable<T> implements Callable<ConnectableObservable<T>> {
        private final int bufferSize;
        private final Observable<T> parent;

        BufferedReplayCallable(Observable<T> observable, int i) {
            this.parent = observable;
            this.bufferSize = i;
        }

        @Override // java.util.concurrent.Callable
        public ConnectableObservable<T> call() {
            return this.parent.replay(this.bufferSize);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$BufferedTimedReplayCallable */
    /* loaded from: classes.dex */
    public static final class BufferedTimedReplayCallable<T> implements Callable<ConnectableObservable<T>> {
        private final int bufferSize;
        private final Observable<T> parent;
        private final Scheduler scheduler;
        private final long time;
        private final TimeUnit unit;

        BufferedTimedReplayCallable(Observable<T> observable, int i, long j, TimeUnit timeUnit, Scheduler scheduler) {
            this.parent = observable;
            this.bufferSize = i;
            this.time = j;
            this.unit = timeUnit;
            this.scheduler = scheduler;
        }

        @Override // java.util.concurrent.Callable
        public ConnectableObservable<T> call() {
            return this.parent.replay(this.bufferSize, this.time, this.unit, this.scheduler);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$TimedReplayCallable */
    /* loaded from: classes.dex */
    public static final class TimedReplayCallable<T> implements Callable<ConnectableObservable<T>> {
        private final Observable<T> parent;
        private final Scheduler scheduler;
        private final long time;
        private final TimeUnit unit;

        TimedReplayCallable(Observable<T> observable, long j, TimeUnit timeUnit, Scheduler scheduler) {
            this.parent = observable;
            this.time = j;
            this.unit = timeUnit;
            this.scheduler = scheduler;
        }

        @Override // java.util.concurrent.Callable
        public ConnectableObservable<T> call() {
            return this.parent.replay(this.time, this.unit, this.scheduler);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableInternalHelper$ReplayFunction */
    /* loaded from: classes.dex */
    public static final class ReplayFunction<T, R> implements Function<Observable<T>, ObservableSource<R>> {
        private final Scheduler scheduler;
        private final Function<? super Observable<T>, ? extends ObservableSource<R>> selector;

        @Override // p005io.reactivex.functions.Function
        public /* bridge */ /* synthetic */ Object apply(Object obj) throws Exception {
            return apply((Observable) ((Observable) obj));
        }

        ReplayFunction(Function<? super Observable<T>, ? extends ObservableSource<R>> function, Scheduler scheduler) {
            this.selector = function;
            this.scheduler = scheduler;
        }

        public ObservableSource<R> apply(Observable<T> observable) throws Exception {
            return Observable.wrap((ObservableSource) ObjectHelper.requireNonNull(this.selector.apply(observable), "The selector returned a null ObservableSource")).observeOn(this.scheduler);
        }
    }
}
