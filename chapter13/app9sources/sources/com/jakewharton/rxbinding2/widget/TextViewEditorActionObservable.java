package com.jakewharton.rxbinding2.widget;

import android.view.KeyEvent;
import android.widget.TextView;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;
import p005io.reactivex.functions.Predicate;

/* loaded from: classes.dex */
final class TextViewEditorActionObservable extends Observable<Integer> {
    private final Predicate<? super Integer> handled;
    private final TextView view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public TextViewEditorActionObservable(TextView textView, Predicate<? super Integer> predicate) {
        this.view = textView;
        this.handled = predicate;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super Integer> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, observer, this.handled);
            observer.onSubscribe(listener);
            this.view.setOnEditorActionListener(listener);
        }
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements TextView.OnEditorActionListener {
        private final Predicate<? super Integer> handled;
        private final Observer<? super Integer> observer;
        private final TextView view;

        Listener(TextView textView, Observer<? super Integer> observer, Predicate<? super Integer> predicate) {
            this.view = textView;
            this.observer = observer;
            this.handled = predicate;
        }

        @Override // android.widget.TextView.OnEditorActionListener
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            try {
                if (isDisposed() || !this.handled.test(Integer.valueOf(i))) {
                    return false;
                }
                this.observer.onNext(Integer.valueOf(i));
                return true;
            } catch (Exception e) {
                this.observer.onError(e);
                dispose();
                return false;
            }
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.setOnEditorActionListener(null);
        }
    }
}
