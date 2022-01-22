package p005io.reactivex.internal.schedulers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.Flowable;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.disposables.Disposables;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.processors.FlowableProcessor;
import p005io.reactivex.processors.UnicastProcessor;

/* renamed from: io.reactivex.internal.schedulers.SchedulerWhen */
/* loaded from: classes.dex */
public class SchedulerWhen extends Scheduler implements Disposable {
    private final Scheduler actualScheduler;
    private Disposable disposable;
    private final FlowableProcessor<Flowable<Completable>> workerProcessor = UnicastProcessor.create().toSerialized();
    static final Disposable SUBSCRIBED = new SubscribedDisposable();
    static final Disposable DISPOSED = Disposables.disposed();

    public SchedulerWhen(Function<Flowable<Flowable<Completable>>, Completable> function, Scheduler scheduler) {
        this.actualScheduler = scheduler;
        try {
            this.disposable = function.apply(this.workerProcessor).subscribe();
        } catch (Throwable th) {
            throw ExceptionHelper.wrapOrThrow(th);
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        this.disposable.dispose();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this.disposable.isDisposed();
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Scheduler.Worker createWorker() {
        Scheduler.Worker createWorker = this.actualScheduler.createWorker();
        FlowableProcessor<T> serialized = UnicastProcessor.create().toSerialized();
        Flowable<Completable> map = serialized.map(new CreateWorkerFunction(createWorker));
        QueueWorker queueWorker = new QueueWorker(serialized, createWorker);
        this.workerProcessor.onNext(map);
        return queueWorker;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$ScheduledAction */
    /* loaded from: classes.dex */
    public static abstract class ScheduledAction extends AtomicReference<Disposable> implements Disposable {
        protected abstract Disposable callActual(Scheduler.Worker worker, CompletableObserver completableObserver);

        ScheduledAction() {
            super(SchedulerWhen.SUBSCRIBED);
        }

        void call(Scheduler.Worker worker, CompletableObserver completableObserver) {
            Disposable disposable = get();
            if (disposable != SchedulerWhen.DISPOSED && disposable == SchedulerWhen.SUBSCRIBED) {
                Disposable callActual = callActual(worker, completableObserver);
                if (!compareAndSet(SchedulerWhen.SUBSCRIBED, callActual)) {
                    callActual.dispose();
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get().isDisposed();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            Disposable disposable;
            Disposable disposable2 = SchedulerWhen.DISPOSED;
            do {
                disposable = get();
                if (disposable == SchedulerWhen.DISPOSED) {
                    return;
                }
            } while (!compareAndSet(disposable, disposable2));
            if (disposable != SchedulerWhen.SUBSCRIBED) {
                disposable.dispose();
            }
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$ImmediateAction */
    /* loaded from: classes.dex */
    static class ImmediateAction extends ScheduledAction {
        private final Runnable action;

        ImmediateAction(Runnable runnable) {
            this.action = runnable;
        }

        @Override // p005io.reactivex.internal.schedulers.SchedulerWhen.ScheduledAction
        protected Disposable callActual(Scheduler.Worker worker, CompletableObserver completableObserver) {
            return worker.schedule(new OnCompletedAction(this.action, completableObserver));
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$DelayedAction */
    /* loaded from: classes.dex */
    static class DelayedAction extends ScheduledAction {
        private final Runnable action;
        private final long delayTime;
        private final TimeUnit unit;

        DelayedAction(Runnable runnable, long j, TimeUnit timeUnit) {
            this.action = runnable;
            this.delayTime = j;
            this.unit = timeUnit;
        }

        @Override // p005io.reactivex.internal.schedulers.SchedulerWhen.ScheduledAction
        protected Disposable callActual(Scheduler.Worker worker, CompletableObserver completableObserver) {
            return worker.schedule(new OnCompletedAction(this.action, completableObserver), this.delayTime, this.unit);
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$OnCompletedAction */
    /* loaded from: classes.dex */
    static class OnCompletedAction implements Runnable {
        final Runnable action;
        final CompletableObserver actionCompletable;

        OnCompletedAction(Runnable runnable, CompletableObserver completableObserver) {
            this.action = runnable;
            this.actionCompletable = completableObserver;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.action.run();
            } finally {
                this.actionCompletable.onComplete();
            }
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$CreateWorkerFunction */
    /* loaded from: classes.dex */
    static final class CreateWorkerFunction implements Function<ScheduledAction, Completable> {
        final Scheduler.Worker actualWorker;

        CreateWorkerFunction(Scheduler.Worker worker) {
            this.actualWorker = worker;
        }

        public Completable apply(ScheduledAction scheduledAction) {
            return new WorkerCompletable(scheduledAction);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$CreateWorkerFunction$WorkerCompletable */
        /* loaded from: classes.dex */
        public final class WorkerCompletable extends Completable {
            final ScheduledAction action;

            WorkerCompletable(ScheduledAction scheduledAction) {
                this.action = scheduledAction;
            }

            @Override // p005io.reactivex.Completable
            protected void subscribeActual(CompletableObserver completableObserver) {
                completableObserver.onSubscribe(this.action);
                this.action.call(CreateWorkerFunction.this.actualWorker, completableObserver);
            }
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$QueueWorker */
    /* loaded from: classes.dex */
    static final class QueueWorker extends Scheduler.Worker {
        private final FlowableProcessor<ScheduledAction> actionProcessor;
        private final Scheduler.Worker actualWorker;
        private final AtomicBoolean unsubscribed = new AtomicBoolean();

        QueueWorker(FlowableProcessor<ScheduledAction> flowableProcessor, Scheduler.Worker worker) {
            this.actionProcessor = flowableProcessor;
            this.actualWorker = worker;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (this.unsubscribed.compareAndSet(false, true)) {
                this.actionProcessor.onComplete();
                this.actualWorker.dispose();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.unsubscribed.get();
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable runnable, long j, @NonNull TimeUnit timeUnit) {
            DelayedAction delayedAction = new DelayedAction(runnable, j, timeUnit);
            this.actionProcessor.onNext(delayedAction);
            return delayedAction;
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable runnable) {
            ImmediateAction immediateAction = new ImmediateAction(runnable);
            this.actionProcessor.onNext(immediateAction);
            return immediateAction;
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$SubscribedDisposable */
    /* loaded from: classes.dex */
    static final class SubscribedDisposable implements Disposable {
        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return false;
        }

        SubscribedDisposable() {
        }
    }
}
