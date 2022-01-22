package com.trello.rxlifecycle2.android;

import android.view.View;
import p005io.reactivex.ObservableEmitter;
import p005io.reactivex.ObservableOnSubscribe;
import p005io.reactivex.android.MainThreadDisposable;

/* loaded from: classes.dex */
final class ViewDetachesOnSubscribe implements ObservableOnSubscribe<Object> {
    static final Object SIGNAL = new Object();
    final View view;

    public ViewDetachesOnSubscribe(View view) {
        this.view = view;
    }

    @Override // p005io.reactivex.ObservableOnSubscribe
    public void subscribe(ObservableEmitter<Object> observableEmitter) throws Exception {
        MainThreadDisposable.verifyMainThread();
        EmitterListener emitterListener = new EmitterListener(observableEmitter);
        observableEmitter.setDisposable(emitterListener);
        this.view.addOnAttachStateChangeListener(emitterListener);
    }

    /* loaded from: classes.dex */
    class EmitterListener extends MainThreadDisposable implements View.OnAttachStateChangeListener {
        final ObservableEmitter<Object> emitter;

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View view) {
        }

        public EmitterListener(ObservableEmitter<Object> observableEmitter) {
            this.emitter = observableEmitter;
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View view) {
            this.emitter.onNext(ViewDetachesOnSubscribe.SIGNAL);
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            ViewDetachesOnSubscribe.this.view.removeOnAttachStateChangeListener(this);
        }
    }
}
