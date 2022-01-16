package p005io.reactivex.internal.schedulers;

import java.util.concurrent.Callable;

/* renamed from: io.reactivex.internal.schedulers.ScheduledDirectTask */
/* loaded from: classes.dex */
public final class ScheduledDirectTask extends AbstractDirectTask implements Callable<Void> {
    private static final long serialVersionUID = 1811839108042568751L;

    @Override // p005io.reactivex.internal.schedulers.AbstractDirectTask, p005io.reactivex.schedulers.SchedulerRunnableIntrospection
    public /* bridge */ /* synthetic */ Runnable getWrappedRunnable() {
        return super.getWrappedRunnable();
    }

    public ScheduledDirectTask(Runnable runnable) {
        super(runnable);
    }

    @Override // java.util.concurrent.Callable
    public Void call() throws Exception {
        this.runner = Thread.currentThread();
        try {
            this.runnable.run();
            return null;
        } finally {
            lazySet(FINISHED);
            this.runner = null;
        }
    }
}
