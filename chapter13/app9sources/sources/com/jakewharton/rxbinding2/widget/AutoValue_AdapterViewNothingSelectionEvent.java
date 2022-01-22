package com.jakewharton.rxbinding2.widget;

import android.support.annotation.NonNull;
import android.widget.AdapterView;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class AutoValue_AdapterViewNothingSelectionEvent extends AdapterViewNothingSelectionEvent {
    private final AdapterView<?> view;

    /* JADX INFO: Access modifiers changed from: package-private */
    public AutoValue_AdapterViewNothingSelectionEvent(AdapterView<?> adapterView) {
        if (adapterView != null) {
            this.view = adapterView;
            return;
        }
        throw new NullPointerException("Null view");
    }

    @Override // com.jakewharton.rxbinding2.widget.AdapterViewSelectionEvent
    @NonNull
    public AdapterView<?> view() {
        return this.view;
    }

    public String toString() {
        return "AdapterViewNothingSelectionEvent{view=" + this.view + "}";
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AdapterViewNothingSelectionEvent) {
            return this.view.equals(((AdapterViewNothingSelectionEvent) obj).view());
        }
        return false;
    }

    public int hashCode() {
        return this.view.hashCode() ^ 1000003;
    }
}
