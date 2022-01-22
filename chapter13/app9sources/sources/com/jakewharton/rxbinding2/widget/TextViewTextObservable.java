package com.jakewharton.rxbinding2.widget;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import com.jakewharton.rxbinding2.InitialValueObservable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;

/* loaded from: classes.dex */
final class TextViewTextObservable extends InitialValueObservable<CharSequence> {
    private final TextView view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public TextViewTextObservable(TextView textView) {
        this.view = textView;
    }

    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    protected void subscribeListener(Observer<? super CharSequence> observer) {
        Listener listener = new Listener(this.view, observer);
        observer.onSubscribe(listener);
        this.view.addTextChangedListener(listener);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    public CharSequence getInitialValue() {
        return this.view.getText();
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements TextWatcher {
        private final Observer<? super CharSequence> observer;
        private final TextView view;

        @Override // android.text.TextWatcher
        public void afterTextChanged(Editable editable) {
        }

        @Override // android.text.TextWatcher
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        Listener(TextView textView, Observer<? super CharSequence> observer) {
            this.view = textView;
            this.observer = observer;
        }

        @Override // android.text.TextWatcher
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (!isDisposed()) {
                this.observer.onNext(charSequence);
            }
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.removeTextChangedListener(this);
        }
    }
}
