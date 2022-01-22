package com.bumptech.glide.load.engine;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.EngineResource;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class ActiveResources {
    private static final int MSG_CLEAN_REF = 1;
    @Nullable

    /* renamed from: cb */
    private volatile DequeuedResourceCallback f50cb;
    @Nullable
    private Thread cleanReferenceQueueThread;
    private final boolean isActiveResourceRetentionAllowed;
    private volatile boolean isShutdown;
    private EngineResource.ResourceListener listener;
    @Nullable
    private ReferenceQueue<EngineResource<?>> resourceReferenceQueue;
    private final Handler mainHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() { // from class: com.bumptech.glide.load.engine.ActiveResources.1
        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message message) {
            if (message.what != 1) {
                return false;
            }
            ActiveResources.this.cleanupActiveReference((ResourceWeakReference) message.obj);
            return true;
        }
    });
    @VisibleForTesting
    final Map<Key, ResourceWeakReference> activeEngineResources = new HashMap();

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    /* loaded from: classes.dex */
    public interface DequeuedResourceCallback {
        void onResourceDequeued();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ActiveResources(boolean z) {
        this.isActiveResourceRetentionAllowed = z;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setListener(EngineResource.ResourceListener resourceListener) {
        this.listener = resourceListener;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void activate(Key key, EngineResource<?> engineResource) {
        ResourceWeakReference put = this.activeEngineResources.put(key, new ResourceWeakReference(key, engineResource, getReferenceQueue(), this.isActiveResourceRetentionAllowed));
        if (put != null) {
            put.reset();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void deactivate(Key key) {
        ResourceWeakReference remove = this.activeEngineResources.remove(key);
        if (remove != null) {
            remove.reset();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Nullable
    public EngineResource<?> get(Key key) {
        ResourceWeakReference resourceWeakReference = this.activeEngineResources.get(key);
        if (resourceWeakReference == null) {
            return null;
        }
        EngineResource<?> engineResource = (EngineResource) resourceWeakReference.get();
        if (engineResource == null) {
            cleanupActiveReference(resourceWeakReference);
        }
        return engineResource;
    }

    void cleanupActiveReference(@NonNull ResourceWeakReference resourceWeakReference) {
        Util.assertMainThread();
        this.activeEngineResources.remove(resourceWeakReference.key);
        if (resourceWeakReference.isCacheable && resourceWeakReference.resource != null) {
            EngineResource<?> engineResource = new EngineResource<>(resourceWeakReference.resource, true, false);
            engineResource.setResourceListener(resourceWeakReference.key, this.listener);
            this.listener.onResourceReleased(resourceWeakReference.key, engineResource);
        }
    }

    private ReferenceQueue<EngineResource<?>> getReferenceQueue() {
        if (this.resourceReferenceQueue == null) {
            this.resourceReferenceQueue = new ReferenceQueue<>();
            this.cleanReferenceQueueThread = new Thread(new Runnable() { // from class: com.bumptech.glide.load.engine.ActiveResources.2
                @Override // java.lang.Runnable
                public void run() {
                    Process.setThreadPriority(10);
                    ActiveResources.this.cleanReferenceQueue();
                }
            }, "glide-active-resources");
            this.cleanReferenceQueueThread.start();
        }
        return this.resourceReferenceQueue;
    }

    void cleanReferenceQueue() {
        while (!this.isShutdown) {
            try {
                this.mainHandler.obtainMessage(1, (ResourceWeakReference) this.resourceReferenceQueue.remove()).sendToTarget();
                DequeuedResourceCallback dequeuedResourceCallback = this.f50cb;
                if (dequeuedResourceCallback != null) {
                    dequeuedResourceCallback.onResourceDequeued();
                }
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @VisibleForTesting
    void setDequeuedResourceCallback(DequeuedResourceCallback dequeuedResourceCallback) {
        this.f50cb = dequeuedResourceCallback;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    public void shutdown() {
        this.isShutdown = true;
        Thread thread = this.cleanReferenceQueueThread;
        if (thread != null) {
            thread.interrupt();
            try {
                this.cleanReferenceQueueThread.join(TimeUnit.SECONDS.toMillis(5));
                if (this.cleanReferenceQueueThread.isAlive()) {
                    throw new RuntimeException("Failed to join in time");
                }
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @VisibleForTesting
    /* loaded from: classes.dex */
    public static final class ResourceWeakReference extends WeakReference<EngineResource<?>> {
        final boolean isCacheable;
        final Key key;
        @Nullable
        Resource<?> resource;

        ResourceWeakReference(@NonNull Key key, @NonNull EngineResource<?> engineResource, @NonNull ReferenceQueue<? super EngineResource<?>> referenceQueue, boolean z) {
            super(engineResource, referenceQueue);
            this.key = (Key) Preconditions.checkNotNull(key);
            this.resource = (!engineResource.isCacheable() || !z) ? null : (Resource) Preconditions.checkNotNull(engineResource.getResource());
            this.isCacheable = engineResource.isCacheable();
        }

        void reset() {
            this.resource = null;
            clear();
        }
    }
}
