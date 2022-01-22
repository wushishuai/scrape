package p005io.reactivex.internal.schedulers;

import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.internal.schedulers.SchedulerMultiWorkerSupport */
/* loaded from: classes.dex */
public interface SchedulerMultiWorkerSupport {

    /* renamed from: io.reactivex.internal.schedulers.SchedulerMultiWorkerSupport$WorkerCallback */
    /* loaded from: classes.dex */
    public interface WorkerCallback {
        void onWorker(int i, @NonNull Scheduler.Worker worker);
    }

    void createWorkers(int i, @NonNull WorkerCallback workerCallback);
}
