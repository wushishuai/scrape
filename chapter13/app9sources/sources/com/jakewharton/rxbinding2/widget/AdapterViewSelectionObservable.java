package com.jakewharton.rxbinding2.widget;

import android.view.View;
import android.widget.AdapterView;
import com.jakewharton.rxbinding2.InitialValueObservable;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;

/* loaded from: classes.dex */
final class AdapterViewSelectionObservable extends InitialValueObservable<AdapterViewSelectionEvent> {
    private final AdapterView<?> view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AdapterViewSelectionObservable(AdapterView<?> adapterView) {
        this.view = adapterView;
    }

    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    protected void subscribeListener(Observer<? super AdapterViewSelectionEvent> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, observer);
            this.view.setOnItemSelectedListener(listener);
            observer.onSubscribe(listener);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    public AdapterViewSelectionEvent getInitialValue() {
        int selectedItemPosition = this.view.getSelectedItemPosition();
        if (selectedItemPosition == -1) {
            return AdapterViewNothingSelectionEvent.create(this.view);
        }
        return AdapterViewItemSelectionEvent.create(this.view, this.view.getSelectedView(), selectedItemPosition, this.view.getSelectedItemId());
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements AdapterView.OnItemSelectedListener {
        private final Observer<? super AdapterViewSelectionEvent> observer;
        private final AdapterView<?> view;

        Listener(AdapterView<?> adapterView, Observer<? super AdapterViewSelectionEvent> observer) {
            this.view = adapterView;
            this.observer = observer;
        }

        @Override // android.widget.AdapterView.OnItemSelectedListener
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
            if (!isDisposed()) {
                this.observer.onNext(AdapterViewItemSelectionEvent.create(adapterView, view, i, j));
            }
        }

        @Override // android.widget.AdapterView.OnItemSelectedListener
        public void onNothingSelected(AdapterView<?> adapterView) {
            if (!isDisposed()) {
                this.observer.onNext(AdapterViewNothingSelectionEvent.create(adapterView));
            }
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.setOnItemSelectedListener(null);
        }
    }
}
