package com.jakewharton.rxbinding2.widget;

import android.view.MenuItem;
import android.widget.PopupMenu;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;

/* loaded from: classes.dex */
final class PopupMenuItemClickObservable extends Observable<MenuItem> {
    private final PopupMenu view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public PopupMenuItemClickObservable(PopupMenu popupMenu) {
        this.view = popupMenu;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super MenuItem> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, observer);
            this.view.setOnMenuItemClickListener(listener);
            observer.onSubscribe(listener);
        }
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements PopupMenu.OnMenuItemClickListener {
        private final Observer<? super MenuItem> observer;
        private final PopupMenu view;

        Listener(PopupMenu popupMenu, Observer<? super MenuItem> observer) {
            this.view = popupMenu;
            this.observer = observer;
        }

        @Override // android.widget.PopupMenu.OnMenuItemClickListener
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
