package com.jakewharton.rxbinding2.widget;

import android.widget.RadioGroup;
import com.jakewharton.rxbinding2.InitialValueObservable;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;

/* loaded from: classes.dex */
final class RadioGroupCheckedChangeObservable extends InitialValueObservable<Integer> {
    private final RadioGroup view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public RadioGroupCheckedChangeObservable(RadioGroup radioGroup) {
        this.view = radioGroup;
    }

    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    protected void subscribeListener(Observer<? super Integer> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, observer);
            this.view.setOnCheckedChangeListener(listener);
            observer.onSubscribe(listener);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    public Integer getInitialValue() {
        return Integer.valueOf(this.view.getCheckedRadioButtonId());
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements RadioGroup.OnCheckedChangeListener {
        private int lastChecked = -1;
        private final Observer<? super Integer> observer;
        private final RadioGroup view;

        Listener(RadioGroup radioGroup, Observer<? super Integer> observer) {
            this.view = radioGroup;
            this.observer = observer;
        }

        @Override // android.widget.RadioGroup.OnCheckedChangeListener
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            if (!isDisposed() && i != this.lastChecked) {
                this.lastChecked = i;
                this.observer.onNext(Integer.valueOf(i));
            }
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.setOnCheckedChangeListener(null);
        }
    }
}
