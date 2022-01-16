package p005io.reactivex.schedulers;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.schedulers.SchedulerRunnableIntrospection */
/* loaded from: classes.dex */
public interface SchedulerRunnableIntrospection {
    @NonNull
    Runnable getWrappedRunnable();
}
