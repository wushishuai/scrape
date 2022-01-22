package com.jakewharton.rxbinding2.widget;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import com.jakewharton.rxbinding2.InitialValueObservable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;

/* loaded from: classes.dex */
final class TextViewTextChangeEventObservable extends InitialValueObservable<TextViewTextChangeEvent> {
    private final TextView view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public TextViewTextChangeEventObservable(TextView textView) {
        this.view = textView;
    }

    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    protected void subscribeListener(Observer<? super TextViewTextChangeEvent> observer) {
        Listener listener = new Listener(this.view, observer);
        observer.onSubscribe(listener);
        this.view.addTextChangedListener(listener);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    public TextViewTextChangeEvent getInitialValue() {
        TextView textView = this.view;
        return TextViewTextChangeEvent.create(textView, textView.getText(), 0, 0, 0);
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements TextWatcher {
        private final Observer<? super TextViewTextChangeEvent> observer;
        private final TextView view;

        @Override // android.text.TextWatcher
        public void afterTextChanged(Editable editable) {
        }

        @Override // android.text.TextWatcher
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        Listener(TextView textView, Observer<? super TextViewTextChangeEvent> observer) {
            this.view = textView;
            this.observer = observer;
        }

        @Override // android.text.TextWatcher
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (!isDisposed()) {
                this.observer.onNext(TextViewTextChangeEvent.create(this.view, charSequence, i, i2, i3));
            }
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.removeTextChangedListener(this);
        }
    }
}
