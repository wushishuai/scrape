package com.jakewharton.rxbinding2.widget;

import android.widget.SearchView;
import com.jakewharton.rxbinding2.InitialValueObservable;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.Observer;
import p005io.reactivex.android.MainThreadDisposable;

/* loaded from: classes.dex */
final class SearchViewQueryTextChangeEventsObservable extends InitialValueObservable<SearchViewQueryTextEvent> {
    private final SearchView view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SearchViewQueryTextChangeEventsObservable(SearchView searchView) {
        this.view = searchView;
    }

    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    protected void subscribeListener(Observer<? super SearchViewQueryTextEvent> observer) {
        if (Preconditions.checkMainThread(observer)) {
            Listener listener = new Listener(this.view, observer);
            this.view.setOnQueryTextListener(listener);
            observer.onSubscribe(listener);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.jakewharton.rxbinding2.InitialValueObservable
    public SearchViewQueryTextEvent getInitialValue() {
        SearchView searchView = this.view;
        return SearchViewQueryTextEvent.create(searchView, searchView.getQuery(), false);
    }

    /* loaded from: classes.dex */
    static final class Listener extends MainThreadDisposable implements SearchView.OnQueryTextListener {
        private final Observer<? super SearchViewQueryTextEvent> observer;
        private final SearchView view;

        Listener(SearchView searchView, Observer<? super SearchViewQueryTextEvent> observer) {
            this.view = searchView;
            this.observer = observer;
        }

        @Override // android.widget.SearchView.OnQueryTextListener
        public boolean onQueryTextChange(String str) {
            if (isDisposed()) {
                return false;
            }
            this.observer.onNext(SearchViewQueryTextEvent.create(this.view, str, false));
            return true;
        }

        @Override // android.widget.SearchView.OnQueryTextListener
        public boolean onQueryTextSubmit(String str) {
            if (isDisposed()) {
                return false;
            }
            this.observer.onNext(SearchViewQueryTextEvent.create(this.view, str, true));
            return true;
        }

        @Override // p005io.reactivex.android.MainThreadDisposable
        protected void onDispose() {
            this.view.setOnQueryTextListener(null);
        }
    }
}
