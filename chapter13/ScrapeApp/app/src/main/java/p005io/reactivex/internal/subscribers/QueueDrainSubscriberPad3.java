package p005io.reactivex.internal.subscribers;

import java.util.concurrent.atomic.AtomicLong;

/* compiled from: QueueDrainSubscriber.java */
/* renamed from: io.reactivex.internal.subscribers.QueueDrainSubscriberPad3 */
/* loaded from: classes.dex */
class QueueDrainSubscriberPad3 extends QueueDrainSubscriberPad2 {
    final AtomicLong requested = new AtomicLong();
}
