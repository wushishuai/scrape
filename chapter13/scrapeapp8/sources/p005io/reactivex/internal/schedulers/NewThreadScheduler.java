package p005io.reactivex.internal.schedulers;

import java.util.concurrent.ThreadFactory;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.internal.schedulers.NewThreadScheduler */
/* loaded from: classes.dex */
public final class NewThreadScheduler extends Scheduler {
    final ThreadFactory threadFactory;
    private static final String THREAD_NAME_PREFIX = "RxNewThreadScheduler";
    private static final String KEY_NEWTHREAD_PRIORITY = "rx2.newthread-priority";
    private static final RxThreadFactory THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX, Math.max(1, Math.min(10, Integer.getInteger(KEY_NEWTHREAD_PRIORITY, 5).intValue())));

    public NewThreadScheduler() {
        this(THREAD_FACTORY);
    }

    public NewThreadScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Scheduler.Worker createWorker() {
        return new NewThreadWorker(this.threadFactory);
    }
}
