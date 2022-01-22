package com.jakewharton.rxbinding2.widget;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class AutoValue_AdapterViewItemClickEvent extends AdapterViewItemClickEvent {
    private final View clickedView;

    /* renamed from: id */
    private final long f69id;
    private final int position;
    private final AdapterView<?> view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AutoValue_AdapterViewItemClickEvent(AdapterView<?> adapterView, View view, int i, long j) {
        if (adapterView != null) {
            this.view = adapterView;
            if (view != null) {
                this.clickedView = view;
                this.position = i;
                this.f69id = j;
                return;
            }
            throw new NullPointerException("Null clickedView");
        }
        throw new NullPointerException("Null view");
    }

    @Override // com.jakewharton.rxbinding2.widget.AdapterViewItemClickEvent
    @NonNull
    public AdapterView<?> view() {
        return this.view;
    }

    @Override // com.jakewharton.rxbinding2.widget.AdapterViewItemClickEvent
    @NonNull
    public View clickedView() {
        return this.clickedView;
    }

    @Override // com.jakewharton.rxbinding2.widget.AdapterViewItemClickEvent
    public int position() {
        return this.position;
    }

    @Override // com.jakewharton.rxbinding2.widget.AdapterViewItemClickEvent
    /* renamed from: id */
    public long mo43id() {
        return this.f69id;
    }

    public String toString() {
        return "AdapterViewItemClickEvent{view=" + this.view + ", clickedView=" + this.clickedView + ", position=" + this.position + ", id=" + this.f69id + "}";
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AdapterViewItemClickEvent)) {
            return false;
        }
        AdapterViewItemClickEvent adapterViewItemClickEvent = (AdapterViewItemClickEvent) obj;
        if (!this.view.equals(adapterViewItemClickEvent.view()) || !this.clickedView.equals(adapterViewItemClickEvent.clickedView()) || this.position != adapterViewItemClickEvent.position() || this.f69id != adapterViewItemClickEvent.mo43id()) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        long j = this.f69id;
        return ((((((this.view.hashCode() ^ 1000003) * 1000003) ^ this.clickedView.hashCode()) * 1000003) ^ this.position) * 1000003) ^ ((int) (j ^ (j >>> 32)));
    }
}
