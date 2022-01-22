package p005io.reactivex.processors;

import org.reactivestreams.Processor;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;

/* renamed from: io.reactivex.processors.FlowableProcessor */
/* loaded from: classes.dex */
public abstract class FlowableProcessor<T> extends Flowable<T> implements Processor<T, T>, FlowableSubscriber<T> {
    @Nullable
    public abstract Throwable getThrowable();

    public abstract boolean hasComplete();

    public abstract boolean hasSubscribers();

    public abstract boolean hasThrowable();

    @CheckReturnValue
    @NonNull
    public final FlowableProcessor<T> toSerialized() {
        if (this instanceof SerializedProcessor) {
            return this;
        }
        return new SerializedProcessor(this);
    }
}
