package p005io.reactivex.internal.fuseable;

import org.reactivestreams.Publisher;

/* renamed from: io.reactivex.internal.fuseable.HasUpstreamPublisher */
/* loaded from: classes.dex */
public interface HasUpstreamPublisher<T> {
    Publisher<T> source();
}
