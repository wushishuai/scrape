package com.jakewharton.rxbinding2.widget;

import android.support.annotation.RequiresApi;
import android.view.MenuItem;
import android.widget.Toolbar;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;

@RequiresApi(21)
/* loaded from: classes.dex */
final class ToolbarItemClickObservable extends Observable<MenuItem> {
    private final Toolbar view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ToolbarItemClickObservable(Toolbar toolbar) {
        this.view = toolbar;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super MenuItem> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, observer);
            observer.onSubscribe(listener);
            this.view.setOnMenuItemClickListener(listener);
        }
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements Toolbar.OnMenuItemClickListener {
        private final Observer<? super MenuItem> observer;
        private final Toolbar view;

        Listener(Toolbar toolbar, Observer<? super MenuItem> observer) {
            this.view = toolbar;
            this.observer = observer;
        }

        @Override // android.widget.Toolbar.OnMenuItemClickListener
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (isDisposed()) {
                return false;
            }
            this.observer.onNext(menuItem);
            return true;
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.setOnMenuItemClickListener(null);
        }
    }
}
