package p005io.reactivex.observables;

import java.util.concurrent.TimeUnit;
import p005io.reactivex.Observable;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.SchedulerSupport;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.functions.Functions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.operators.observable.ObservableAutoConnect;
import p005io.reactivex.internal.operators.observable.ObservableRefCount;
import p005io.reactivex.internal.util.ConnectConsumer;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.schedulers.Schedulers;

/* renamed from: io.reactivex.observables.ConnectableObservable */
/* loaded from: classes.dex */
public abstract class ConnectableObservable<T> extends Observable<T> {
    public abstract void connect(@NonNull Consumer<? super Disposable> consumer);

    public final Disposable connect() {
        ConnectConsumer cc = new ConnectConsumer();
        connect(cc);
        return cc.disposable;
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    @NonNull
    public Observable<T> refCount() {
        return RxJavaPlugins.onAssembly(new ObservableRefCount(this));
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @CheckReturnValue
    public final Observable<T> refCount(int subscriberCount) {
        return refCount(subscriberCount, 0, TimeUnit.NANOSECONDS, Schedulers.trampoline());
    }

    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @CheckReturnValue
    public final Observable<T> refCount(long timeout, TimeUnit unit) {
        return refCount(1, timeout, unit, Schedulers.computation());
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public final Observable<T> refCount(long timeout, TimeUnit unit, Scheduler scheduler) {
        return refCount(1, timeout, unit, scheduler);
    }

    @SchedulerSupport(SchedulerSupport.COMPUTATION)
    @CheckReturnValue
    public final Observable<T> refCount(int subscriberCount, long timeout, TimeUnit unit) {
        return refCount(subscriberCount, timeout, unit, Schedulers.computation());
    }

    @SchedulerSupport(SchedulerSupport.CUSTOM)
    @CheckReturnValue
    public final Observable<T> refCount(int subscriberCount, long timeout, TimeUnit unit, Scheduler scheduler) {
        ObjectHelper.verifyPositive(subscriberCount, "subscriberCount");
        ObjectHelper.requireNonNull(unit, "unit is null");
        ObjectHelper.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new ObservableRefCount(this, subscriberCount, timeout, unit, scheduler));
    }

    @NonNull
    public Observable<T> autoConnect() {
        return autoConnect(1);
    }

    @NonNull
    public Observable<T> autoConnect(int numberOfSubscribers) {
        return autoConnect(numberOfSubscribers, Functions.emptyConsumer());
    }

    @NonNull
    public Observable<T> autoConnect(int numberOfSubscribers, @NonNull Consumer<? super Disposable> connection) {
        if (numberOfSubscribers > 0) {
            return RxJavaPlugins.onAssembly(new ObservableAutoConnect(this, numberOfSubscribers, connection));
        }
        connect(connection);
        return RxJavaPlugins.onAssembly((ConnectableObservable) this);
    }
}
