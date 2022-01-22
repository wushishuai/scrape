package com.jakewharton.rxbinding2.view;

import android.view.MotionEvent;
import android.view.View;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;
import p005io.reactivex.functions.Predicate;

/* loaded from: classes.dex */
final class ViewHoverObservable extends Observable<MotionEvent> {
    private final Predicate<? super MotionEvent> handled;
    private final View view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ViewHoverObservable(View view, Predicate<? super MotionEvent> predicate) {
        this.view = view;
        this.handled = predicate;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super MotionEvent> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, this.handled, observer);
            observer.onSubscribe(listener);
            this.view.setOnHoverListener(listener);
        }
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements View.OnHoverListener {
        private final Predicate<? super MotionEvent> handled;
        private final Observer<? super MotionEvent> observer;
        private final View view;

        Listener(View view, Predicate<? super MotionEvent> predicate, Observer<? super MotionEvent> observer) {
            this.view = view;
            this.handled = predicate;
            this.observer = observer;
        }

        @Override // android.view.View.OnHoverListener
        public boolean onHover(View view, MotionEvent motionEvent) {
            if (isDisposed()) {
                return false;
            }
            try {
                if (!this.handled.test(motionEvent)) {
                    return false;
                }
                this.observer.onNext(motionEvent);
                return true;
            } catch (Exception e) {
                this.observer.onError(e);
                dispose();
                return false;
            }
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.setOnHoverListener(null);
        }
    }
}
