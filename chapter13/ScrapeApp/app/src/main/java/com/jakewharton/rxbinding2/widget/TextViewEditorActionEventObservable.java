package com.jakewharton.rxbinding2.widget;

import android.view.KeyEvent;
import android.widget.TextView;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;
import p005io.reactivex.functions.Predicate;

/* loaded from: classes.dex */
final class TextViewEditorActionEventObservable extends Observable<TextViewEditorActionEvent> {
    private final Predicate<? super TextViewEditorActionEvent> handled;
    private final TextView view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public TextViewEditorActionEventObservable(TextView view, Predicate<? super TextViewEditorActionEvent> handled) {
        this.view = view;
        this.handled = handled;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super TextViewEditorActionEvent> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, observer, this.handled);
            observer.onSubscribe(listener);
            this.view.setOnEditorActionListener(listener);
        }
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements TextView.OnEditorActionListener {
        private final Predicate<? super TextViewEditorActionEvent> handled;
        private final Observer<? super TextViewEditorActionEvent> observer;
        private final TextView view;

        Listener(TextView view, Observer<? super TextViewEditorActionEvent> observer, Predicate<? super TextViewEditorActionEvent> handled) {
            this.view = view;
            this.observer = observer;
            this.handled = handled;
        }

        @Override // android.widget.TextView.OnEditorActionListener
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            TextViewEditorActionEvent event = TextViewEditorActionEvent.create(this.view, actionId, keyEvent);
            try {
                if (isDisposed() || !this.handled.test(event)) {
                    return false;
                }
                this.observer.onNext(event);
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
