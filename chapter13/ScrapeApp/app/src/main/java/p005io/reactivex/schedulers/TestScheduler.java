package p005io.reactivex.schedulers;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.disposables.Disposables;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.schedulers.TestScheduler */
/* loaded from: classes.dex */
public final class TestScheduler extends Scheduler {
    long counter;
    final Queue<TimedRunnable> queue = new PriorityBlockingQueue(11);
    volatile long time;

    public TestScheduler() {
    }

    public TestScheduler(long delayTime, TimeUnit unit) {
        this.time = unit.toNanos(delayTime);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.schedulers.TestScheduler$TimedRunnable */
    /* loaded from: classes.dex */
    public static final class TimedRunnable implements Comparable<TimedRunnable> {
        final long count;
        final Runnable run;
        final TestWorker scheduler;
        final long time;

        TimedRunnable(TestWorker scheduler, long time, Runnable run, long count) {
            this.time = time;
            this.run = run;
            this.scheduler = scheduler;
            this.count = count;
        }

        @Override // java.lang.Object
        public String toString() {
            return String.format("TimedRunnable(time = %d, run = %s)", Long.valueOf(this.time), this.run.toString());
        }

        public int compareTo(TimedRunnable o) {
            long j = this.time;
            long j2 = o.time;
            if (j == j2) {
                return ObjectHelper.compare(this.count, o.count);
            }
            return ObjectHelper.compare(j, j2);
        }
    }

    @Override // p005io.reactivex.Scheduler
    public long now(@NonNull TimeUnit unit) {
        return unit.convert(this.time, TimeUnit.NANOSECONDS);
    }

    public void advanceTimeBy(long delayTime, TimeUnit unit) {
        advanceTimeTo(this.time + unit.toNanos(delayTime), TimeUnit.NANOSECONDS);
    }

    public void advanceTimeTo(long delayTime, TimeUnit unit) {
        triggerActions(unit.toNanos(delayTime));
    }

    public void triggerActions() {
        triggerActions(this.time);
    }

    private void triggerActions(long targetTimeInNanoseconds) {
        while (true) {
            TimedRunnable current = this.queue.peek();
            if (current == null || current.time > targetTimeInNanoseconds) {
                break;
            }
            this.time = current.time == 0 ? this.time : current.time;
            this.queue.remove(current);
            if (!current.scheduler.disposed) {
                current.run.run();
            }
        }
        this.time = targetTimeInNanoseconds;
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Scheduler.Worker createWorker() {
        return new TestWorker();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.schedulers.TestScheduler$TestWorker */
    /* loaded from: classes.dex */
    public final class TestWorker extends Scheduler.Worker {
        volatile boolean disposed;

        TestWorker() {
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.disposed = true;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable run, long delayTime, @NonNull TimeUnit unit) {
            if (this.disposed) {
                return EmptyDisposable.INSTANCE;
            }
            long nanos = unit.toNanos(delayTime) + TestScheduler.this.time;
            TestScheduler testScheduler = TestScheduler.this;
            long j = testScheduler.counter;
            testScheduler.counter = 1 + j;
            TimedRunnable timedAction = new TimedRunnable(this, nanos, run, j);
            TestScheduler.this.queue.add(timedAction);
            return Disposables.fromRunnable(new QueueRemove(timedAction));
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable run) {
            if (this.disposed) {
                return EmptyDisposable.INSTANCE;
            }
            TestScheduler testScheduler = TestScheduler.this;
            long j = testScheduler.counter;
            testScheduler.counter = 1 + j;
            TimedRunnable timedAction = new TimedRunnable(this, 0, run, j);
            TestScheduler.this.queue.add(timedAction);
            return Disposables.fromRunnable(new QueueRemove(timedAction));
        }

        @Override // p005io.reactivex.Scheduler.Worker
        public long now(@NonNull TimeUnit unit) {
            return TestScheduler.this.now(unit);
        }

        /* renamed from: io.reactivex.schedulers.TestScheduler$TestWorker$QueueRemove */
        /* loaded from: classes.dex */
        final class QueueRemove implements Runnable {
            final TimedRunnable timedAction;

            QueueRemove(TimedRunnable timedAction) {
                this.timedAction = timedAction;
            }

            @Override // java.lang.Runnable
            public void run() {
                TestScheduler.this.queue.remove(this.timedAction);
            }
        }
    }
}
