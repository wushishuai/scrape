package p005io.reactivex;

import android.support.p003v7.widget.ActivityChooserView;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Publisher;
import p005io.reactivex.annotations.BackpressureKind;
import p005io.reactivex.annotations.BackpressureSupport;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.SchedulerSupport;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Action;
import p005io.reactivex.functions.BiConsumer;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.functions.BiPredicate;
import p005io.reactivex.functions.BooleanSupplier;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.functions.Function;
import p005io.reactivex.functions.Function3;
import p005io.reactivex.functions.Function4;
import p005io.reactivex.functions.Function5;
import p005io.reactivex.functions.Function6;
import p005io.reactivex.functions.Function7;
import p005io.reactivex.functions.Function8;
import p005io.reactivex.functions.Function9;
import p005io.reactivex.functions.Predicate;
import p005io.reactivex.internal.functions.Functions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.FuseToFlowable;
import p005io.reactivex.internal.fuseable.FuseToMaybe;
import p005io.reactivex.internal.fuseable.FuseToObservable;
import p005io.reactivex.internal.observers.BiConsumerSingleObserver;
import p005io.reactivex.internal.observers.BlockingMultiObserver;
import p005io.reactivex.internal.observers.ConsumerSingleObserver;
import p005io.reactivex.internal.observers.FutureSingleObserver;
import p005io.reactivex.internal.operators.completable.CompletableFromSingle;
import p005io.reactivex.internal.operators.completable.CompletableToFlowable;
import p005io.reactivex.internal.operators.flowable.FlowableConcatMap;
import p005io.reactivex.internal.operators.flowable.FlowableConcatMapPublisher;
import p005io.reactivex.internal.operators.flowable.FlowableFlatMapPublisher;
import p005io.reactivex.internal.operators.flowable.FlowableSingleSingle;
import p005io.reactivex.internal.operators.maybe.MaybeFilterSingle;
import p005io.reactivex.internal.operators.maybe.MaybeFromSingle;
import p005io.reactivex.internal.operators.mixed.SingleFlatMapObservable;
import p005io.reactivex.internal.operators.observable.ObservableConcatMap;
import p005io.reactivex.internal.operators.observable.ObservableSingleSingle;
import p005io.reactivex.internal.operators.single.SingleAmb;
import p005io.reactivex.internal.operators.single.SingleCache;
import p005io.reactivex.internal.operators.single.SingleContains;
import p005io.reactivex.internal.operators.single.SingleCreate;
import p005io.reactivex.internal.operators.single.SingleDefer;
import p005io.reactivex.internal.operators.single.SingleDelay;
import p005io.reactivex.internal.operators.single.SingleDelayWithCompletable;
import p005io.reactivex.internal.operators.single.SingleDelayWithObservable;
import p005io.reactivex.internal.operators.single.SingleDelayWithPublisher;
import p005io.reactivex.internal.operators.single.SingleDelayWithSingle;
import p005io.reactivex.internal.operators.single.SingleDetach;
import p005io.reactivex.internal.operators.single.SingleDoAfterSuccess;
import p005io.reactivex.internal.operators.single.SingleDoAfterTerminate;
import p005io.reactivex.internal.operators.single.SingleDoFinally;
import p005io.reactivex.internal.operators.single.SingleDoOnDispose;
import p005io.reactivex.internal.operators.single.SingleDoOnError;
import p005io.reactivex.internal.operators.single.SingleDoOnEvent;
import p005io.reactivex.internal.operators.single.SingleDoOnSubscribe;
import p005io.reactivex.internal.operators.single.SingleDoOnSuccess;
import p005io.reactivex.internal.operators.single.SingleEquals;
import p005io.reactivex.internal.operators.single.SingleError;
import p005io.reactivex.internal.operators.single.SingleFlatMap;
import p005io.reactivex.internal.operators.single.SingleFlatMapCompletable;
import p005io.reactivex.internal.operators.single.SingleFlatMapIterableFlowable;
import p005io.reactivex.internal.operators.single.SingleFlatMapIterableObservable;
import p005io.reactivex.internal.operators.single.SingleFlatMapMaybe;
import p005io.reactivex.internal.operators.single.SingleFlatMapPublisher;
import p005io.reactivex.internal.operators.single.SingleFromCallable;
import p005io.reactivex.internal.operators.single.SingleFromPublisher;
import p005io.reactivex.internal.operators.single.SingleFromUnsafeSource;
import p005io.reactivex.internal.operators.single.SingleHide;
import p005io.reactivex.internal.operators.single.SingleInternalHelper;
import p005io.reactivex.internal.operators.single.SingleJust;
import p005io.reactivex.internal.operators.single.SingleLift;
import p005io.reactivex.internal.operators.single.SingleMap;
import p005io.reactivex.internal.operators.single.SingleNever;
import p005io.reactivex.internal.operators.single.SingleObserveOn;
import p005io.reactivex.internal.operators.single.SingleOnErrorReturn;
import p005io.reactivex.internal.operators.single.SingleResumeNext;
import p005io.reactivex.internal.operators.single.SingleSubscribeOn;
import p005io.reactivex.internal.operators.single.SingleTakeUntil;
import p005io.reactivex.internal.operators.single.SingleTimeout;
import p005io.reactivex.internal.operators.single.SingleTimer;
import p005io.reactivex.internal.operators.single.SingleToFlowable;
import p005io.reactivex.internal.operators.single.SingleToObservable;
import p005io.reactivex.internal.operators.single.SingleUnsubscribeOn;
import p005io.reactivex.internal.operators.single.SingleUsing;
import p005io.reactivex.internal.operators.single.SingleZipArray;
import p005io.reactivex.internal.operators.single.SingleZipIterable;
import p005io.reactivex.internal.util.ErrorMode;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.observers.TestObserver;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.schedulers.Schedulers;

/* renamed from: io.reactivex.Single */
/* loaded from: classes.dex */
public abstract class Single<T> implements SingleSource<T> {
    protected abstract void subscribeActual(@NonNull SingleObserver<? super T> singleObserver);

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> amb(Iterable<? extends SingleSource<? extends T>> iterable) {
        ObjectHelper.requireNonNull(iterable, "sources is null");
        return RxJavaPlugins.onAssembly(new SingleAmb(null, iterable));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> ambArray(SingleSource<? extends T>... singleSourceArr) {
        if (singleSourceArr.length == 0) {
            return error(SingleInternalHelper.emptyThrower());
        }
        if (singleSourceArr.length == 1) {
            return wrap(singleSourceArr[0]);
        }
        return RxJavaPlugins.onAssembly(new SingleAmb(singleSourceArr, null));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> concat(Iterable<? extends SingleSource<? extends T>> iterable) {
        return concat(Flowable.fromIterable(iterable));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Observable<T> concat(ObservableSource<? extends SingleSource<? extends T>> observableSource) {
        ObjectHelper.requireNonNull(observableSource, "sources is null");
        return RxJavaPlugins.onAssembly(new ObservableConcatMap(observableSource, SingleInternalHelper.toObservable(), 2, ErrorMode.IMMEDIATE));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> concat(Publisher<? extends SingleSource<? extends T>> publisher) {
        return concat(publisher, 2);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> concat(Publisher<? extends SingleSource<? extends T>> publisher, int i) {
        ObjectHelper.requireNonNull(publisher, "sources is null");
        ObjectHelper.verifyPositive(i, "prefetch");
        return RxJavaPlugins.onAssembly(new FlowableConcatMapPublisher(publisher, SingleInternalHelper.toFlowable(), i, ErrorMode.IMMEDIATE));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> concat(SingleSource<? extends T> singleSource, SingleSource<? extends T> singleSource2) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        return concat(Flowable.fromArray(singleSource, singleSource2));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> concat(SingleSource<? extends T> singleSource, SingleSource<? extends T> singleSource2, SingleSource<? extends T> singleSource3) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        return concat(Flowable.fromArray(singleSource, singleSource2, singleSource3));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> concat(SingleSource<? extends T> singleSource, SingleSource<? extends T> singleSource2, SingleSource<? extends T> singleSource3, SingleSource<? extends T> singleSource4) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        ObjectHelper.requireNonNull(singleSource4, "source4 is null");
        return concat(Flowable.fromArray(singleSource, singleSource2, singleSource3, singleSource4));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> concatArray(SingleSource<? extends T>... singleSourceArr) {
        return RxJavaPlugins.onAssembly(new FlowableConcatMap(Flowable.fromArray(singleSourceArr), SingleInternalHelper.toFlowable(), 2, ErrorMode.BOUNDARY));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> concatArrayEager(SingleSource<? extends T>... singleSourceArr) {
        return Flowable.fromArray(singleSourceArr).concatMapEager(SingleInternalHelper.toFlowable());
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> concatEager(Publisher<? extends SingleSource<? extends T>> publisher) {
        return Flowable.fromPublisher(publisher).concatMapEager(SingleInternalHelper.toFlowable());
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> concatEager(Iterable<? extends SingleSource<? extends T>> iterable) {
        return Flowable.fromIterable(iterable).concatMapEager(SingleInternalHelper.toFlowable());
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> create(SingleOnSubscribe<T> singleOnSubscribe) {
        ObjectHelper.requireNonNull(singleOnSubscribe, "source is null");
        return RxJavaPlugins.onAssembly(new SingleCreate(singleOnSubscribe));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> defer(Callable<? extends SingleSource<? extends T>> callable) {
        ObjectHelper.requireNonNull(callable, "singleSupplier is null");
        return RxJavaPlugins.onAssembly(new SingleDefer(callable));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> error(Callable<? extends Throwable> callable) {
        ObjectHelper.requireNonNull(callable, "errorSupplier is null");
        return RxJavaPlugins.onAssembly(new SingleError(callable));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> error(Throwable th) {
        ObjectHelper.requireNonNull(th, "error is null");
        return error(Functions.justCallable(th));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> fromCallable(Callable<? extends T> callable) {
        ObjectHelper.requireNonNull(callable, "callable is null");
        return RxJavaPlugins.onAssembly(new SingleFromCallable(callable));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> fromFuture(Future<? extends T> future) {
        return toSingle(Flowable.fromFuture(future));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> fromFuture(Future<? extends T> future, long j, TimeUnit timeUnit) {
        return toSingle(Flowable.fromFuture(future, j, timeUnit));
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public static <T> Single<T> fromFuture(Future<? extends T> future, long j, TimeUnit timeUnit, Scheduler scheduler) {
        return toSingle(Flowable.fromFuture(future, j, timeUnit, scheduler));
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public static <T> Single<T> fromFuture(Future<? extends T> future, Scheduler scheduler) {
        return toSingle(Flowable.fromFuture(future, scheduler));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.UNBOUNDED_IN)
    @CheckReturnValue
    public static <T> Single<T> fromPublisher(Publisher<? extends T> publisher) {
        ObjectHelper.requireNonNull(publisher, "publisher is null");
        return RxJavaPlugins.onAssembly(new SingleFromPublisher(publisher));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> fromObservable(ObservableSource<? extends T> observableSource) {
        ObjectHelper.requireNonNull(observableSource, "observableSource is null");
        return RxJavaPlugins.onAssembly(new ObservableSingleSingle(observableSource, null));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> just(T t) {
        ObjectHelper.requireNonNull(t, "value is null");
        return RxJavaPlugins.onAssembly(new SingleJust(t));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> merge(Iterable<? extends SingleSource<? extends T>> iterable) {
        return merge(Flowable.fromIterable(iterable));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> merge(Publisher<? extends SingleSource<? extends T>> publisher) {
        ObjectHelper.requireNonNull(publisher, "sources is null");
        return RxJavaPlugins.onAssembly(new FlowableFlatMapPublisher(publisher, SingleInternalHelper.toFlowable(), false, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED, Flowable.bufferSize()));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> merge(SingleSource<? extends SingleSource<? extends T>> singleSource) {
        ObjectHelper.requireNonNull(singleSource, "source is null");
        return RxJavaPlugins.onAssembly(new SingleFlatMap(singleSource, Functions.identity()));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> merge(SingleSource<? extends T> singleSource, SingleSource<? extends T> singleSource2) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        return merge(Flowable.fromArray(singleSource, singleSource2));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> merge(SingleSource<? extends T> singleSource, SingleSource<? extends T> singleSource2, SingleSource<? extends T> singleSource3) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        return merge(Flowable.fromArray(singleSource, singleSource2, singleSource3));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> merge(SingleSource<? extends T> singleSource, SingleSource<? extends T> singleSource2, SingleSource<? extends T> singleSource3, SingleSource<? extends T> singleSource4) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        ObjectHelper.requireNonNull(singleSource4, "source4 is null");
        return merge(Flowable.fromArray(singleSource, singleSource2, singleSource3, singleSource4));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> mergeDelayError(Iterable<? extends SingleSource<? extends T>> iterable) {
        return mergeDelayError(Flowable.fromIterable(iterable));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> mergeDelayError(Publisher<? extends SingleSource<? extends T>> publisher) {
        ObjectHelper.requireNonNull(publisher, "sources is null");
        return RxJavaPlugins.onAssembly(new FlowableFlatMapPublisher(publisher, SingleInternalHelper.toFlowable(), true, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED, Flowable.bufferSize()));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> mergeDelayError(SingleSource<? extends T> singleSource, SingleSource<? extends T> singleSource2) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        return mergeDelayError(Flowable.fromArray(singleSource, singleSource2));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> mergeDelayError(SingleSource<? extends T> singleSource, SingleSource<? extends T> singleSource2, SingleSource<? extends T> singleSource3) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        return mergeDelayError(Flowable.fromArray(singleSource, singleSource2, singleSource3));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public static <T> Flowable<T> mergeDelayError(SingleSource<? extends T> singleSource, SingleSource<? extends T> singleSource2, SingleSource<? extends T> singleSource3, SingleSource<? extends T> singleSource4) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        ObjectHelper.requireNonNull(singleSource4, "source4 is null");
        return mergeDelayError(Flowable.fromArray(singleSource, singleSource2, singleSource3, singleSource4));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> never() {
        return RxJavaPlugins.onAssembly(SingleNever.INSTANCE);
    }

    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @CheckReturnValue
    public static Single<Long> timer(long j, TimeUnit timeUnit) {
        return timer(j, timeUnit, Schedulers.computation());
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public static Single<Long> timer(long j, TimeUnit timeUnit, Scheduler scheduler) {
        ObjectHelper.requireNonNull(timeUnit, "unit is null");
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new SingleTimer(j, timeUnit, scheduler));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<Boolean> equals(SingleSource<? extends T> singleSource, SingleSource<? extends T> singleSource2) {
        ObjectHelper.requireNonNull(singleSource, "first is null");
        ObjectHelper.requireNonNull(singleSource2, "second is null");
        return RxJavaPlugins.onAssembly(new SingleEquals(singleSource, singleSource2));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> unsafeCreate(SingleSource<T> singleSource) {
        ObjectHelper.requireNonNull(singleSource, "onSubscribe is null");
        if (!(singleSource instanceof Single)) {
            return RxJavaPlugins.onAssembly(new SingleFromUnsafeSource(singleSource));
        }
        throw new IllegalArgumentException("unsafeCreate(Single) should be upgraded");
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T, U> Single<T> using(Callable<U> callable, Function<? super U, ? extends SingleSource<? extends T>> function, Consumer<? super U> consumer) {
        return using(callable, function, consumer, true);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T, U> Single<T> using(Callable<U> callable, Function<? super U, ? extends SingleSource<? extends T>> function, Consumer<? super U> consumer, boolean z) {
        ObjectHelper.requireNonNull(callable, "resourceSupplier is null");
        ObjectHelper.requireNonNull(function, "singleFunction is null");
        ObjectHelper.requireNonNull(consumer, "disposer is null");
        return RxJavaPlugins.onAssembly(new SingleUsing(callable, function, consumer, z));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T> Single<T> wrap(SingleSource<T> singleSource) {
        ObjectHelper.requireNonNull(singleSource, "source is null");
        if (singleSource instanceof Single) {
            return RxJavaPlugins.onAssembly((Single) singleSource);
        }
        return RxJavaPlugins.onAssembly(new SingleFromUnsafeSource(singleSource));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T, R> Single<R> zip(Iterable<? extends SingleSource<? extends T>> iterable, Function<? super Object[], ? extends R> function) {
        ObjectHelper.requireNonNull(function, "zipper is null");
        ObjectHelper.requireNonNull(iterable, "sources is null");
        return RxJavaPlugins.onAssembly(new SingleZipIterable(iterable, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T1, T2, R> Single<R> zip(SingleSource<? extends T1> singleSource, SingleSource<? extends T2> singleSource2, BiFunction<? super T1, ? super T2, ? extends R> biFunction) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        return zipArray(Functions.toFunction(biFunction), singleSource, singleSource2);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T1, T2, T3, R> Single<R> zip(SingleSource<? extends T1> singleSource, SingleSource<? extends T2> singleSource2, SingleSource<? extends T3> singleSource3, Function3<? super T1, ? super T2, ? super T3, ? extends R> function3) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        return zipArray(Functions.toFunction(function3), singleSource, singleSource2, singleSource3);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T1, T2, T3, T4, R> Single<R> zip(SingleSource<? extends T1> singleSource, SingleSource<? extends T2> singleSource2, SingleSource<? extends T3> singleSource3, SingleSource<? extends T4> singleSource4, Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> function4) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        ObjectHelper.requireNonNull(singleSource4, "source4 is null");
        return zipArray(Functions.toFunction(function4), singleSource, singleSource2, singleSource3, singleSource4);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T1, T2, T3, T4, T5, R> Single<R> zip(SingleSource<? extends T1> singleSource, SingleSource<? extends T2> singleSource2, SingleSource<? extends T3> singleSource3, SingleSource<? extends T4> singleSource4, SingleSource<? extends T5> singleSource5, Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> function5) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        ObjectHelper.requireNonNull(singleSource4, "source4 is null");
        ObjectHelper.requireNonNull(singleSource5, "source5 is null");
        return zipArray(Functions.toFunction(function5), singleSource, singleSource2, singleSource3, singleSource4, singleSource5);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T1, T2, T3, T4, T5, T6, R> Single<R> zip(SingleSource<? extends T1> singleSource, SingleSource<? extends T2> singleSource2, SingleSource<? extends T3> singleSource3, SingleSource<? extends T4> singleSource4, SingleSource<? extends T5> singleSource5, SingleSource<? extends T6> singleSource6, Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> function6) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        ObjectHelper.requireNonNull(singleSource4, "source4 is null");
        ObjectHelper.requireNonNull(singleSource5, "source5 is null");
        ObjectHelper.requireNonNull(singleSource6, "source6 is null");
        return zipArray(Functions.toFunction(function6), singleSource, singleSource2, singleSource3, singleSource4, singleSource5, singleSource6);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T1, T2, T3, T4, T5, T6, T7, R> Single<R> zip(SingleSource<? extends T1> singleSource, SingleSource<? extends T2> singleSource2, SingleSource<? extends T3> singleSource3, SingleSource<? extends T4> singleSource4, SingleSource<? extends T5> singleSource5, SingleSource<? extends T6> singleSource6, SingleSource<? extends T7> singleSource7, Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> function7) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        ObjectHelper.requireNonNull(singleSource4, "source4 is null");
        ObjectHelper.requireNonNull(singleSource5, "source5 is null");
        ObjectHelper.requireNonNull(singleSource6, "source6 is null");
        ObjectHelper.requireNonNull(singleSource7, "source7 is null");
        return zipArray(Functions.toFunction(function7), singleSource, singleSource2, singleSource3, singleSource4, singleSource5, singleSource6, singleSource7);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Single<R> zip(SingleSource<? extends T1> singleSource, SingleSource<? extends T2> singleSource2, SingleSource<? extends T3> singleSource3, SingleSource<? extends T4> singleSource4, SingleSource<? extends T5> singleSource5, SingleSource<? extends T6> singleSource6, SingleSource<? extends T7> singleSource7, SingleSource<? extends T8> singleSource8, Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> function8) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        ObjectHelper.requireNonNull(singleSource4, "source4 is null");
        ObjectHelper.requireNonNull(singleSource5, "source5 is null");
        ObjectHelper.requireNonNull(singleSource6, "source6 is null");
        ObjectHelper.requireNonNull(singleSource7, "source7 is null");
        ObjectHelper.requireNonNull(singleSource8, "source8 is null");
        return zipArray(Functions.toFunction(function8), singleSource, singleSource2, singleSource3, singleSource4, singleSource5, singleSource6, singleSource7, singleSource8);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Single<R> zip(SingleSource<? extends T1> singleSource, SingleSource<? extends T2> singleSource2, SingleSource<? extends T3> singleSource3, SingleSource<? extends T4> singleSource4, SingleSource<? extends T5> singleSource5, SingleSource<? extends T6> singleSource6, SingleSource<? extends T7> singleSource7, SingleSource<? extends T8> singleSource8, SingleSource<? extends T9> singleSource9, Function9<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? extends R> function9) {
        ObjectHelper.requireNonNull(singleSource, "source1 is null");
        ObjectHelper.requireNonNull(singleSource2, "source2 is null");
        ObjectHelper.requireNonNull(singleSource3, "source3 is null");
        ObjectHelper.requireNonNull(singleSource4, "source4 is null");
        ObjectHelper.requireNonNull(singleSource5, "source5 is null");
        ObjectHelper.requireNonNull(singleSource6, "source6 is null");
        ObjectHelper.requireNonNull(singleSource7, "source7 is null");
        ObjectHelper.requireNonNull(singleSource8, "source8 is null");
        ObjectHelper.requireNonNull(singleSource9, "source9 is null");
        return zipArray(Functions.toFunction(function9), singleSource, singleSource2, singleSource3, singleSource4, singleSource5, singleSource6, singleSource7, singleSource8, singleSource9);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public static <T, R> Single<R> zipArray(Function<? super Object[], ? extends R> function, SingleSource<? extends T>... singleSourceArr) {
        ObjectHelper.requireNonNull(function, "zipper is null");
        ObjectHelper.requireNonNull(singleSourceArr, "sources is null");
        if (singleSourceArr.length == 0) {
            return error(new NoSuchElementException());
        }
        return RxJavaPlugins.onAssembly(new SingleZipArray(singleSourceArr, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> ambWith(SingleSource<? extends T> singleSource) {
        ObjectHelper.requireNonNull(singleSource, "other is null");
        return ambArray(this, singleSource);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    /* renamed from: as */
    public final <R> R m31as(@NonNull SingleConverter<T, ? extends R> singleConverter) {
        return (R) ((SingleConverter) ObjectHelper.requireNonNull(singleConverter, "converter is null")).apply(this);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> hide() {
        return RxJavaPlugins.onAssembly(new SingleHide(this));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <R> Single<R> compose(SingleTransformer<? super T, ? extends R> singleTransformer) {
        return wrap(((SingleTransformer) ObjectHelper.requireNonNull(singleTransformer, "transformer is null")).apply(this));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> cache() {
        return RxJavaPlugins.onAssembly(new SingleCache(this));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <U> Single<U> cast(Class<? extends U> cls) {
        ObjectHelper.requireNonNull(cls, "clazz is null");
        return (Single<U>) map(Functions.castFunction(cls));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final Flowable<T> concatWith(SingleSource<? extends T> singleSource) {
        return concat(this, singleSource);
    }

    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @CheckReturnValue
    public final Single<T> delay(long j, TimeUnit timeUnit) {
        return delay(j, timeUnit, Schedulers.computation(), false);
    }

    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @CheckReturnValue
    public final Single<T> delay(long j, TimeUnit timeUnit, boolean z) {
        return delay(j, timeUnit, Schedulers.computation(), z);
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public final Single<T> delay(long j, TimeUnit timeUnit, Scheduler scheduler) {
        return delay(j, timeUnit, scheduler, false);
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public final Single<T> delay(long j, TimeUnit timeUnit, Scheduler scheduler, boolean z) {
        ObjectHelper.requireNonNull(timeUnit, "unit is null");
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new SingleDelay(this, j, timeUnit, scheduler, z));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> delaySubscription(CompletableSource completableSource) {
        ObjectHelper.requireNonNull(completableSource, "other is null");
        return RxJavaPlugins.onAssembly(new SingleDelayWithCompletable(this, completableSource));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <U> Single<T> delaySubscription(SingleSource<U> singleSource) {
        ObjectHelper.requireNonNull(singleSource, "other is null");
        return RxJavaPlugins.onAssembly(new SingleDelayWithSingle(this, singleSource));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <U> Single<T> delaySubscription(ObservableSource<U> observableSource) {
        ObjectHelper.requireNonNull(observableSource, "other is null");
        return RxJavaPlugins.onAssembly(new SingleDelayWithObservable(this, observableSource));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final <U> Single<T> delaySubscription(Publisher<U> publisher) {
        ObjectHelper.requireNonNull(publisher, "other is null");
        return RxJavaPlugins.onAssembly(new SingleDelayWithPublisher(this, publisher));
    }

    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @CheckReturnValue
    public final Single<T> delaySubscription(long j, TimeUnit timeUnit) {
        return delaySubscription(j, timeUnit, Schedulers.computation());
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public final Single<T> delaySubscription(long j, TimeUnit timeUnit, Scheduler scheduler) {
        return delaySubscription(Observable.timer(j, timeUnit, scheduler));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> doAfterSuccess(Consumer<? super T> consumer) {
        ObjectHelper.requireNonNull(consumer, "doAfterSuccess is null");
        return RxJavaPlugins.onAssembly(new SingleDoAfterSuccess(this, consumer));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> doAfterTerminate(Action action) {
        ObjectHelper.requireNonNull(action, "onAfterTerminate is null");
        return RxJavaPlugins.onAssembly(new SingleDoAfterTerminate(this, action));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> doFinally(Action action) {
        ObjectHelper.requireNonNull(action, "onFinally is null");
        return RxJavaPlugins.onAssembly(new SingleDoFinally(this, action));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> doOnSubscribe(Consumer<? super Disposable> consumer) {
        ObjectHelper.requireNonNull(consumer, "onSubscribe is null");
        return RxJavaPlugins.onAssembly(new SingleDoOnSubscribe(this, consumer));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> doOnSuccess(Consumer<? super T> consumer) {
        ObjectHelper.requireNonNull(consumer, "onSuccess is null");
        return RxJavaPlugins.onAssembly(new SingleDoOnSuccess(this, consumer));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> doOnEvent(BiConsumer<? super T, ? super Throwable> biConsumer) {
        ObjectHelper.requireNonNull(biConsumer, "onEvent is null");
        return RxJavaPlugins.onAssembly(new SingleDoOnEvent(this, biConsumer));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> doOnError(Consumer<? super Throwable> consumer) {
        ObjectHelper.requireNonNull(consumer, "onError is null");
        return RxJavaPlugins.onAssembly(new SingleDoOnError(this, consumer));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> doOnDispose(Action action) {
        ObjectHelper.requireNonNull(action, "onDispose is null");
        return RxJavaPlugins.onAssembly(new SingleDoOnDispose(this, action));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Maybe<T> filter(Predicate<? super T> predicate) {
        ObjectHelper.requireNonNull(predicate, "predicate is null");
        return RxJavaPlugins.onAssembly(new MaybeFilterSingle(this, predicate));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <R> Single<R> flatMap(Function<? super T, ? extends SingleSource<? extends R>> function) {
        ObjectHelper.requireNonNull(function, "mapper is null");
        return RxJavaPlugins.onAssembly(new SingleFlatMap(this, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <R> Maybe<R> flatMapMaybe(Function<? super T, ? extends MaybeSource<? extends R>> function) {
        ObjectHelper.requireNonNull(function, "mapper is null");
        return RxJavaPlugins.onAssembly(new SingleFlatMapMaybe(this, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final <R> Flowable<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> function) {
        ObjectHelper.requireNonNull(function, "mapper is null");
        return RxJavaPlugins.onAssembly(new SingleFlatMapPublisher(this, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final <U> Flowable<U> flattenAsFlowable(Function<? super T, ? extends Iterable<? extends U>> function) {
        ObjectHelper.requireNonNull(function, "mapper is null");
        return RxJavaPlugins.onAssembly(new SingleFlatMapIterableFlowable(this, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <U> Observable<U> flattenAsObservable(Function<? super T, ? extends Iterable<? extends U>> function) {
        ObjectHelper.requireNonNull(function, "mapper is null");
        return RxJavaPlugins.onAssembly(new SingleFlatMapIterableObservable(this, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <R> Observable<R> flatMapObservable(Function<? super T, ? extends ObservableSource<? extends R>> function) {
        ObjectHelper.requireNonNull(function, "mapper is null");
        return RxJavaPlugins.onAssembly(new SingleFlatMapObservable(this, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Completable flatMapCompletable(Function<? super T, ? extends CompletableSource> function) {
        ObjectHelper.requireNonNull(function, "mapper is null");
        return RxJavaPlugins.onAssembly(new SingleFlatMapCompletable(this, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final T blockingGet() {
        BlockingMultiObserver blockingMultiObserver = new BlockingMultiObserver();
        subscribe(blockingMultiObserver);
        return (T) blockingMultiObserver.blockingGet();
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <R> Single<R> lift(SingleOperator<? extends R, ? super T> singleOperator) {
        ObjectHelper.requireNonNull(singleOperator, "onLift is null");
        return RxJavaPlugins.onAssembly(new SingleLift(this, singleOperator));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <R> Single<R> map(Function<? super T, ? extends R> function) {
        ObjectHelper.requireNonNull(function, "mapper is null");
        return RxJavaPlugins.onAssembly(new SingleMap(this, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<Boolean> contains(Object obj) {
        return contains(obj, ObjectHelper.equalsPredicate());
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<Boolean> contains(Object obj, BiPredicate<Object, Object> biPredicate) {
        ObjectHelper.requireNonNull(obj, "value is null");
        ObjectHelper.requireNonNull(biPredicate, "comparer is null");
        return RxJavaPlugins.onAssembly(new SingleContains(this, obj, biPredicate));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final Flowable<T> mergeWith(SingleSource<? extends T> singleSource) {
        return merge(this, singleSource);
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public final Single<T> observeOn(Scheduler scheduler) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new SingleObserveOn(this, scheduler));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> onErrorReturn(Function<Throwable, ? extends T> function) {
        ObjectHelper.requireNonNull(function, "resumeFunction is null");
        return RxJavaPlugins.onAssembly(new SingleOnErrorReturn(this, function, null));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> onErrorReturnItem(T t) {
        ObjectHelper.requireNonNull(t, "value is null");
        return RxJavaPlugins.onAssembly(new SingleOnErrorReturn(this, null, t));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> onErrorResumeNext(Single<? extends T> single) {
        ObjectHelper.requireNonNull(single, "resumeSingleInCaseOfError is null");
        return onErrorResumeNext(Functions.justFunction(single));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> onErrorResumeNext(Function<? super Throwable, ? extends SingleSource<? extends T>> function) {
        ObjectHelper.requireNonNull(function, "resumeFunctionInCaseOfError is null");
        return RxJavaPlugins.onAssembly(new SingleResumeNext(this, function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> onTerminateDetach() {
        return RxJavaPlugins.onAssembly(new SingleDetach(this));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final Flowable<T> repeat() {
        return toFlowable().repeat();
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final Flowable<T> repeat(long j) {
        return toFlowable().repeat(j);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final Flowable<T> repeatWhen(Function<? super Flowable<Object>, ? extends Publisher<?>> function) {
        return toFlowable().repeatWhen(function);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final Flowable<T> repeatUntil(BooleanSupplier booleanSupplier) {
        return toFlowable().repeatUntil(booleanSupplier);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> retry() {
        return toSingle(toFlowable().retry());
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> retry(long j) {
        return toSingle(toFlowable().retry(j));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> retry(BiPredicate<? super Integer, ? super Throwable> biPredicate) {
        return toSingle(toFlowable().retry(biPredicate));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> retry(long j, Predicate<? super Throwable> predicate) {
        return toSingle(toFlowable().retry(j, predicate));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> retry(Predicate<? super Throwable> predicate) {
        return toSingle(toFlowable().retry(predicate));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> retryWhen(Function<? super Flowable<Throwable>, ? extends Publisher<?>> function) {
        return toSingle(toFlowable().retryWhen(function));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    public final Disposable subscribe() {
        return subscribe(Functions.emptyConsumer(), Functions.ON_ERROR_MISSING);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Disposable subscribe(BiConsumer<? super T, ? super Throwable> biConsumer) {
        ObjectHelper.requireNonNull(biConsumer, "onCallback is null");
        BiConsumerSingleObserver biConsumerSingleObserver = new BiConsumerSingleObserver(biConsumer);
        subscribe(biConsumerSingleObserver);
        return biConsumerSingleObserver;
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Disposable subscribe(Consumer<? super T> consumer) {
        return subscribe(consumer, Functions.ON_ERROR_MISSING);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Disposable subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumer2) {
        ObjectHelper.requireNonNull(consumer, "onSuccess is null");
        ObjectHelper.requireNonNull(consumer2, "onError is null");
        ConsumerSingleObserver consumerSingleObserver = new ConsumerSingleObserver(consumer, consumer2);
        subscribe(consumerSingleObserver);
        return consumerSingleObserver;
    }

    @Override // p005io.reactivex.SingleSource
    @SchedulerSupport(SchedulerSupport.NONE)
    public final void subscribe(SingleObserver<? super T> singleObserver) {
        ObjectHelper.requireNonNull(singleObserver, "subscriber is null");
        SingleObserver<? super T> onSubscribe = RxJavaPlugins.onSubscribe(this, singleObserver);
        ObjectHelper.requireNonNull(onSubscribe, "The RxJavaPlugins.onSubscribe hook returned a null SingleObserver. Please check the handler provided to RxJavaPlugins.setOnSingleSubscribe for invalid null returns. Further reading: https://github.com/ReactiveX/RxJava/wiki/Plugins");
        try {
            subscribeActual(onSubscribe);
        } catch (NullPointerException e) {
            throw e;
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            NullPointerException nullPointerException = new NullPointerException("subscribeActual failed");
            nullPointerException.initCause(th);
            throw nullPointerException;
        }
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <E extends SingleObserver<? super T>> E subscribeWith(E e) {
        subscribe(e);
        return e;
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public final Single<T> subscribeOn(Scheduler scheduler) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new SingleSubscribeOn(this, scheduler));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Single<T> takeUntil(CompletableSource completableSource) {
        ObjectHelper.requireNonNull(completableSource, "other is null");
        return takeUntil(new CompletableToFlowable(completableSource));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final <E> Single<T> takeUntil(Publisher<E> publisher) {
        ObjectHelper.requireNonNull(publisher, "other is null");
        return RxJavaPlugins.onAssembly(new SingleTakeUntil(this, publisher));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <E> Single<T> takeUntil(SingleSource<? extends E> singleSource) {
        ObjectHelper.requireNonNull(singleSource, "other is null");
        return takeUntil(new SingleToFlowable(singleSource));
    }

    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @CheckReturnValue
    public final Single<T> timeout(long j, TimeUnit timeUnit) {
        return timeout0(j, timeUnit, Schedulers.computation(), null);
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public final Single<T> timeout(long j, TimeUnit timeUnit, Scheduler scheduler) {
        return timeout0(j, timeUnit, scheduler, null);
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public final Single<T> timeout(long j, TimeUnit timeUnit, Scheduler scheduler, SingleSource<? extends T> singleSource) {
        ObjectHelper.requireNonNull(singleSource, "other is null");
        return timeout0(j, timeUnit, scheduler, singleSource);
    }

    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @CheckReturnValue
    public final Single<T> timeout(long j, TimeUnit timeUnit, SingleSource<? extends T> singleSource) {
        ObjectHelper.requireNonNull(singleSource, "other is null");
        return timeout0(j, timeUnit, Schedulers.computation(), singleSource);
    }

    private Single<T> timeout0(long j, TimeUnit timeUnit, Scheduler scheduler, SingleSource<? extends T> singleSource) {
        ObjectHelper.requireNonNull(timeUnit, "unit is null");
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new SingleTimeout(this, j, timeUnit, scheduler, singleSource));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    /* renamed from: to */
    public final <R> R m30to(Function<? super Single<T>, R> function) {
        try {
            return (R) ((Function) ObjectHelper.requireNonNull(function, "convert is null")).apply(this);
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            throw ExceptionHelper.wrapOrThrow(th);
        }
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    @Deprecated
    public final Completable toCompletable() {
        return RxJavaPlugins.onAssembly(new CompletableFromSingle(this));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Completable ignoreElement() {
        return RxJavaPlugins.onAssembly(new CompletableFromSingle(this));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @BackpressureSupport(BackpressureKind.FULL)
    @CheckReturnValue
    public final Flowable<T> toFlowable() {
        if (this instanceof FuseToFlowable) {
            return ((FuseToFlowable) this).fuseToFlowable();
        }
        return RxJavaPlugins.onAssembly(new SingleToFlowable(this));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Future<T> toFuture() {
        return (Future) subscribeWith(new FutureSingleObserver());
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Maybe<T> toMaybe() {
        if (this instanceof FuseToMaybe) {
            return ((FuseToMaybe) this).fuseToMaybe();
        }
        return RxJavaPlugins.onAssembly(new MaybeFromSingle(this));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Observable<T> toObservable() {
        if (this instanceof FuseToObservable) {
            return ((FuseToObservable) this).fuseToObservable();
        }
        return RxJavaPlugins.onAssembly(new SingleToObservable(this));
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public final Single<T> unsubscribeOn(Scheduler scheduler) {
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new SingleUnsubscribeOn(this, scheduler));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final <U, R> Single<R> zipWith(SingleSource<U> singleSource, BiFunction<? super T, ? super U, ? extends R> biFunction) {
        return zip(this, singleSource, biFunction);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final TestObserver<T> test() {
        TestObserver<T> testObserver = new TestObserver<>();
        subscribe(testObserver);
        return testObserver;
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final TestObserver<T> test(boolean z) {
        TestObserver<T> testObserver = new TestObserver<>();
        if (z) {
            testObserver.cancel();
        }
        subscribe(testObserver);
        return testObserver;
    }

    private static <T> Single<T> toSingle(Flowable<T> flowable) {
        return RxJavaPlugins.onAssembly(new FlowableSingleSingle(flowable, null));
    }
}
