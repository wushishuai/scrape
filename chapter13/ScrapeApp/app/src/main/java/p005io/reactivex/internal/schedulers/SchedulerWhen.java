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

    public SchedulerWhen(Function<Flowable<Flowable<Completable>>, Completable> combine, Scheduler actualScheduler) {
        this.actualScheduler = actualScheduler;
        try {
            this.disposable = combine.apply(this.workerProcessor).subscribe();
        } catch (Throwable e) {
            throw ExceptionHelper.wrapOrThrow(e);
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
        Scheduler.Worker actualWorker = this.actualScheduler.createWorker();
        FlowableProcessor<T> serialized = UnicastProcessor.create().toSerialized();
        Flowable<Completable> actions = serialized.map(new CreateWorkerFunction(actualWorker));
        Scheduler.Worker worker = new QueueWorker(serialized, actualWorker);
        this.workerProcessor.onNext(actions);
        return worker;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$ScheduledAction */
    /* loaded from: classes.dex */
    public static abstract class ScheduledAction extends AtomicReference<Disposable> implements Disposable {
        protected abstract Disposable callActual(Scheduler.Worker worker, CompletableObserver completableObserver);

        ScheduledAction() {
            super(SchedulerWhen.SUBSCRIBED);
        }

        void call(Scheduler.Worker actualWorker, CompletableObserver actionCompletable) {
            Disposable oldState = get();
            if (oldState != SchedulerWhen.DISPOSED && oldState == SchedulerWhen.SUBSCRIBED) {
                Disposable newState = callActual(actualWorker, actionCompletable);
                if (!compareAndSet(SchedulerWhen.SUBSCRIBED, newState)) {
                    newState.dispose();
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get().isDisposed();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            Disposable oldState;
            Disposable newState = SchedulerWhen.DISPOSED;
            do {
                oldState = get();
                if (oldState == SchedulerWhen.DISPOSED) {
                    return;
                }
            } while (!compareAndSet(oldState, newState));
            if (oldState != SchedulerWhen.SUBSCRIBED) {
                oldState.dispose();
            }
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$ImmediateAction */
    /* loaded from: classes.dex */
    static class ImmediateAction extends ScheduledAction {
        private final Runnable action;

        ImmediateAction(Runnable action) {
            this.action = action;
        }

        @Override // p005io.reactivex.internal.schedulers.SchedulerWhen.ScheduledAction
        protected Disposable callActual(Scheduler.Worker actualWorker, CompletableObserver actionCompletable) {
            return actualWorker.schedule(new OnCompletedAction(this.action, actionCompletable));
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$DelayedAction */
    /* loaded from: classes.dex */
    static class DelayedAction extends ScheduledAction {
        private final Runnable action;
        private final long delayTime;
        private final TimeUnit unit;

        DelayedAction(Runnable action, long delayTime, TimeUnit unit) {
            this.action = action;
            this.delayTime = delayTime;
            this.unit = unit;
        }

        @Override // p005io.reactivex.internal.schedulers.SchedulerWhen.ScheduledAction
        protected Disposable callActual(Scheduler.Worker actualWorker, CompletableObserver actionCompletable) {
            return actualWorker.schedule(new OnCompletedAction(this.action, actionCompletable), this.delayTime, this.unit);
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$OnCompletedAction */
    /* loaded from: classes.dex */
    static class OnCompletedAction implements Runnable {
        final Runnable action;
        final CompletableObserver actionCompletable;

        OnCompletedAction(Runnable action, CompletableObserver actionCompletable) {
            this.action = action;
            this.actionCompletable = actionCompletable;
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

        CreateWorkerFunction(Scheduler.Worker actualWorker) {
            this.actualWorker = actualWorker;
        }

        public Completable apply(ScheduledAction action) {
            return new WorkerCompletable(action);
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$CreateWorkerFunction$WorkerCompletable */
        /* loaded from: classes.dex */
        public final class WorkerCompletable extends Completable {
            final ScheduledAction action;

            WorkerCompletable(ScheduledAction action) {
                this.action = action;
            }

            @Override // p005io.reactivex.Completable
            protected void subscribeActual(CompletableObserver actionCompletable) {
                actionCompletable.onSubscribe(this.action);
                this.action.call(CreateWorkerFunction.this.actualWorker, actionCompletable);
            }
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$QueueWorker */
    /* loaded from: classes.dex */
    static final class QueueWorker extends Scheduler.Worker {
        private final FlowableProcessor<ScheduledAction> actionProcessor;
        private final Scheduler.Worker actualWorker;
        private final AtomicBoolean unsubscribed = new AtomicBoolean();

        QueueWorker(FlowableProcessor<ScheduledAction> actionProcessor, Scheduler.Worker actualWorker) {
            this.actionProcessor = actionProcessor;
            this.actualWorker = actualWorker;
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
        public Disposable schedule(@NonNull Runnable action, long delayTime, @NonNull TimeUnit unit) {
            DelayedAction delayedAction = new DelayedAction(action, delayTime, unit);
            this.actionProcessor.onNext(delayedAction);
            return delayedAction;
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable action) {
            ImmediateAction immediateAction = new ImmediateAction(action);
            this.actionProcessor.onNext(immediateAction);
            return immediateAction;
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SchedulerWhen$SubscribedDisposable */
    /* loaded from: classes.dex */
    static final class SubscribedDisposable implements Disposable {
        SubscribedDisposable() {
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return false;
        }
    }
}
