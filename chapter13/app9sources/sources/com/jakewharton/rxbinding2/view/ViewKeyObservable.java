package com.jakewharton.rxbinding2.view;

import android.view.KeyEvent;
import android.view.View;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;
import p005io.reactivex.functions.Predicate;

/* loaded from: classes.dex */
final class ViewKeyObservable extends Observable<KeyEvent> {
    private final Predicate<? super KeyEvent> handled;
    private final View view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ViewKeyObservable(View view, Predicate<? super KeyEvent> predicate) {
        this.view = view;
        this.handled = predicate;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super KeyEvent> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, this.handled, observer);
            observer.onSubscribe(listener);
            this.view.setOnKeyListener(listener);
        }
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements View.OnKeyListener {
        private final Predicate<? super KeyEvent> handled;
        private final Observer<? super KeyEvent> observer;
        private final View view;

        Listener(View view, Predicate<? super KeyEvent> predicate, Observer<? super KeyEvent> observer) {
            this.view = view;
            this.handled = predicate;
            this.observer = observer;
        }

        @Override // android.view.View.OnKeyListener
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (isDisposed()) {
                return false;
            }
            try {
                if (!this.handled.test(keyEvent)) {
                    return false;
                }
                this.observer.onNext(keyEvent);
                return true;
            } catch (Exception e) {
                this.observer.onError(e);
                dispose();
                return false;
            }
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.setOnKeyListener(null);
        }
    }
}
