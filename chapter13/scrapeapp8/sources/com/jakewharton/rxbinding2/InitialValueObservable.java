package com.jakewharton.rxbinding2;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;

/* loaded from: classes.dex */
public abstract class InitialValueObservable<T> extends Observable<T> {
    protected abstract T getInitialValue();

    protected abstract void subscribeListener(Observer<? super T> observer);

    @Override // p005io.reactivex.Observable
    protected final void subscribeActual(Observer<? super T> observer) {
        subscribeListener(observer);
        observer.onNext(getInitialValue());
    }

    public final Observable<T> skipInitialValue() {
        return new Skipped();
    }

    /* loaded from: classes.dex */
    private final class Skipped extends Observable<T> {
        Skipped() {
        }

        @Override // p005io.reactivex.Observable
        protected void subscribeActual(Observer<? super T> observer) {
            InitialValueObservable.this.subscribeListener(observer);
        }
    }
}
