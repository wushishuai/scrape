package p005io.reactivex.internal.fuseable;

import p005io.reactivex.FlowableSubscriber;

/* renamed from: io.reactivex.internal.fuseable.ConditionalSubscriber */
/* loaded from: classes.dex */
public interface ConditionalSubscriber<T> extends FlowableSubscriber<T> {
    boolean tryOnNext(T t);
}
