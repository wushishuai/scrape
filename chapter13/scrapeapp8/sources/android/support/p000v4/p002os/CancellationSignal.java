package android.support.p000v4.p002os;

import android.os.Build;

/* renamed from: android.support.v4.os.CancellationSignal */
/* loaded from: classes.dex */
public final class CancellationSignal {
    private boolean mCancelInProgress;
    private Object mCancellationSignalObj;
    private boolean mIsCanceled;
    private OnCancelListener mOnCancelListener;

    /* renamed from: android.support.v4.os.CancellationSignal$OnCancelListener */
    /* loaded from: classes.dex */
    public interface OnCancelListener {
        void onCancel();
    }

    public boolean isCanceled() {
        boolean z;
        synchronized (this) {
            z = this.mIsCanceled;
        }
        return z;
    }

    public void throwIfCanceled() {
        if (isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    public void cancel() {
        synchronized (this) {
            try {
                try {
                    if (!this.mIsCanceled) {
                        this.mIsCanceled = true;
                        this.mCancelInProgress = true;
                        OnCancelListener listener = this.mOnCancelListener;
                        Object obj = this.mCancellationSignalObj;
                        if (listener != null) {
                            try {
                                listener.onCancel();
                            } catch (Throwable th) {
                                synchronized (this) {
                                    try {
                                        this.mCancelInProgress = false;
                                        notifyAll();
                                        throw th;
                                    } catch (Throwable th2) {
                                        throw th2;
                                    }
                                }
                            }
                        }
                        if (obj != null && Build.VERSION.SDK_INT >= 16) {
                            ((android.os.CancellationSignal) obj).cancel();
                        }
                        synchronized (this) {
                            try {
                                this.mCancelInProgress = false;
                                notifyAll();
                            } catch (Throwable th3) {
                                throw th3;
                            }
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
            }
        }
    }

    public void setOnCancelListener(OnCancelListener listener) {
        synchronized (this) {
            waitForCancelFinishedLocked();
            if (this.mOnCancelListener != listener) {
                this.mOnCancelListener = listener;
                if (this.mIsCanceled && listener != null) {
                    listener.onCancel();
                }
            }
        }
    }

    public Object getCancellationSignalObject() {
        Object obj;
        if (Build.VERSION.SDK_INT < 16) {
            return null;
        }
        synchronized (this) {
            if (this.mCancellationSignalObj == null) {
                this.mCancellationSignalObj = new android.os.CancellationSignal();
                if (this.mIsCanceled) {
                    ((android.os.CancellationSignal) this.mCancellationSignalObj).cancel();
                }
            }
            obj = this.mCancellationSignalObj;
        }
        return obj;
    }

    private void waitForCancelFinishedLocked() {
        while (this.mCancelInProgress) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
}
