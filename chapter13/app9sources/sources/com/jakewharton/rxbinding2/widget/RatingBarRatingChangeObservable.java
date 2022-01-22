package com.jakewharton.rxbinding2.widget;

import android.widget.RatingBar;
import com.jakewharton.rxbinding2.InitialValueObservable;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;

/* loaded from: classes.dex */
final class RatingBarRatingChangeObservable extends InitialValueObservable<Float> {
    private final RatingBar view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public RatingBarRatingChangeObservable(RatingBar ratingBar) {
        this.view = ratingBar;
    }

    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    protected void subscribeListener(Observer<? super Float> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, observer);
            this.view.setOnRatingBarChangeListener(listener);
            observer.onSubscribe(listener);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    public Float getInitialValue() {
        return Float.valueOf(this.view.getRating());
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements RatingBar.OnRatingBarChangeListener {
        private final Observer<? super Float> observer;
        private final RatingBar view;

        Listener(RatingBar ratingBar, Observer<? super Float> observer) {
            this.view = ratingBar;
            this.observer = observer;
        }

        @Override // android.widget.RatingBar.OnRatingBarChangeListener
        public void onRatingChanged(RatingBar ratingBar, float f, boolean z) {
            if (!isDisposed()) {
                this.observer.onNext(Float.valueOf(f));
            }
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.setOnRatingBarChangeListener(null);
        }
    }
}
