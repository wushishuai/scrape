package p006me.goldze.mvvmhabit.http.download;

import p005io.reactivex.observers.DisposableObserver;

/* renamed from: me.goldze.mvvmhabit.http.download.DownLoadSubscriber */
/* loaded from: classes.dex */
public class DownLoadSubscriber<T> extends DisposableObserver<T> {
    private ProgressCallBack fileCallBack;

    public DownLoadSubscriber(ProgressCallBack fileCallBack) {
        this.fileCallBack = fileCallBack;
    }

    @Override // p005io.reactivex.observers.DisposableObserver
    public void onStart() {
        super.onStart();
        ProgressCallBack progressCallBack = this.fileCallBack;
        if (progressCallBack != null) {
            progressCallBack.onStart();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        ProgressCallBack progressCallBack = this.fileCallBack;
        if (progressCallBack != null) {
            progressCallBack.onCompleted();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable e) {
        ProgressCallBack progressCallBack = this.fileCallBack;
        if (progressCallBack != null) {
            progressCallBack.onError(e);
        }
    }

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        ProgressCallBack progressCallBack = this.fileCallBack;
        if (progressCallBack != null) {
            progressCallBack.onSuccess(t);
        }
    }
}
