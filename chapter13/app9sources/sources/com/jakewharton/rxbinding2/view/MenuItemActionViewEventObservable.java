package com.jakewharton.rxbinding2.view;

import android.view.MenuItem;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;
import p005io.reactivex.functions.Predicate;

/* loaded from: classes.dex */
final class MenuItemActionViewEventObservable extends Observable<MenuItemActionViewEvent> {
    private final Predicate<? super MenuItemActionViewEvent> handled;
    private final MenuItem menuItem;

    /* JADX INFO: Access modifiers changed from: package-private */
    public MenuItemActionViewEventObservable(MenuItem menuItem, Predicate<? super MenuItemActionViewEvent> predicate) {
        this.menuItem = menuItem;
        this.handled = predicate;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super MenuItemActionViewEvent> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.menuItem, this.handled, observer);
            observer.onSubscribe(listener);
            this.menuItem.setOnActionExpandListener(listener);
        }
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements MenuItem.OnActionExpandListener {
        private final Predicate<? super MenuItemActionViewEvent> handled;
        private final MenuItem menuItem;
        private final Observer<? super MenuItemActionViewEvent> observer;

        Listener(MenuItem menuItem, Predicate<? super MenuItemActionViewEvent> predicate, Observer<? super MenuItemActionViewEvent> observer) {
            this.menuItem = menuItem;
            this.handled = predicate;
            this.observer = observer;
        }

        @Override // android.view.MenuItem.OnActionExpandListener
        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return onEvent(MenuItemActionViewExpandEvent.create(menuItem));
        }

        @Override // android.view.MenuItem.OnActionExpandListener
        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            return onEvent(MenuItemActionViewCollapseEvent.create(menuItem));
        }

        private boolean onEvent(MenuItemActionViewEvent menuItemActionViewEvent) {
            if (isDisposed()) {
                return false;
            }
            try {
                if (!this.handled.test(menuItemActionViewEvent)) {
                    return false;
                }
                this.observer.onNext(menuItemActionViewEvent);
                return true;
            } catch (Exception e) {
                this.observer.onError(e);
                dispose();
                return false;
            }
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.menuItem.setOnActionExpandListener(null);
        }
    }
}
