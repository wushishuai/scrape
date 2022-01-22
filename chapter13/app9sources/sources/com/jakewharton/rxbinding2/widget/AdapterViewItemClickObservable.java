package com.jakewharton.rxbinding2.widget;

import android.view.View;
import android.widget.AdapterView;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;

/* loaded from: classes.dex */
final class AdapterViewItemClickObservable extends Observable<Integer> {
    private final AdapterView<?> view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AdapterViewItemClickObservable(AdapterView<?> adapterView) {
        this.view = adapterView;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super Integer> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, observer);
            observer.onSubscribe(listener);
            this.view.setOnItemClickListener(listener);
        }
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements AdapterView.OnItemClickListener {
        private final Observer<? super Integer> observer;
        private final AdapterView<?> view;

        Listener(AdapterView<?> adapterView, Observer<? super Integer> observer) {
            this.view = adapterView;
            this.observer = observer;
        }

        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            if (!isDisposed()) {
                this.observer.onNext(Integer.valueOf(i));
            }
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.setOnItemClickListener(null);
        }
    }
}
