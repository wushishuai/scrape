package com.jakewharton.rxbinding2.widget;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class AutoValue_AdapterViewItemLongClickEvent extends AdapterViewItemLongClickEvent {
    private final View clickedView;

    /* renamed from: id */
    private final long f70id;
    private final int position;
    private final AdapterView<?> view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AutoValue_AdapterViewItemLongClickEvent(AdapterView<?> adapterView, View view, int i, long j) {
        if (adapterView != null) {
            this.view = adapterView;
            if (view != null) {
                this.clickedView = view;
                this.position = i;
                this.f70id = j;
                return;
            }
            throw new NullPointerException("Null clickedView");
        }
        throw new NullPointerException("Null view");
    }

    @Override // com.jakewharton.rxbinding2.widget.AdapterViewItemLongClickEvent
    @NonNull
    public AdapterView<?> view() {
        return this.view;
    }

    @Override // com.jakewharton.rxbinding2.widget.AdapterViewItemLongClickEvent
    @NonNull
    public View clickedView() {
        return this.clickedView;
    }

    @Override // com.jakewharton.rxbinding2.widget.AdapterViewItemLongClickEvent
    public int position() {
        return this.position;
    }

    @Override // com.jakewharton.rxbinding2.widget.AdapterViewItemLongClickEvent
    /* renamed from: id */
    public long mo42id() {
        return this.f70id;
    }

    public String toString() {
        return "AdapterViewItemLongClickEvent{view=" + this.view + ", clickedView=" + this.clickedView + ", position=" + this.position + ", id=" + this.f70id + "}";
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AdapterViewItemLongClickEvent)) {
            return false;
        }
        AdapterViewItemLongClickEvent adapterViewItemLongClickEvent = (AdapterViewItemLongClickEvent) obj;
        if (!this.view.equals(adapterViewItemLongClickEvent.view()) || !this.clickedView.equals(adapterViewItemLongClickEvent.clickedView()) || this.position != adapterViewItemLongClickEvent.position() || this.f70id != adapterViewItemLongClickEvent.mo42id()) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        long j = this.f70id;
        return ((((((this.view.hashCode() ^ 1000003) * 1000003) ^ this.clickedView.hashCode()) * 1000003) ^ this.position) * 1000003) ^ ((int) (j ^ (j >>> 32)));
    }
}
